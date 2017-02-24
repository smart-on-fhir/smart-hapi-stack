package org.smartplatforms.demo.interceptor;


import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.method.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilder;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Set;
import org.mitre.oauth2.introspectingfilter.IntrospectingTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * Created by LarsKristian on 12.09.2016.
 * https://tools.ietf.org/html/rfc7662
 * http://docs.smarthealthit.org/authorization/scopes-and-launch-context/
 */
public class OAuthAuthorizationInterceptor extends AuthorizationInterceptor {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OAuthAuthorizationInterceptor.class);

	@Autowired
	private IntrospectingTokenService introspectingTokenService;

	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
		if (theRequestDetails.getCompleteUrl().endsWith("/metadata")) {
			return new RuleBuilder()
					.allowAll()
					.build();
		}

		// Check if request has authorization header and Bearer token
		if (theRequestDetails.getHeader("Authorization") == null) {
			// Throw an HTTP 401
			throw new AuthenticationException("Missing Authorization header value");
		} else if (!theRequestDetails.getHeader("Authorization").toUpperCase().startsWith(OAuth2AccessToken.BEARER_TYPE.toUpperCase())) {
			logger.error("Bearer not found (do not log in production!) = " + theRequestDetails.getHeader("Authorization"));
			throw new AuthenticationException("Missing Bearer token in Authorization header (must start with 'Bearer')");
		}

		String authHeader = theRequestDetails.getHeader("Authorization");
		String accessToken = authHeader.split(" ")[1];

		boolean isJWTValid;
		boolean userIsAdmin = false;
		IdDt userIdPatientId = null;

		final Set<Scope> scopes;
		try {
			OAuth2Authentication oAuth2Authentication = introspectingTokenService.loadAuthentication(accessToken);
			if (oAuth2Authentication != null) {
				userIsAdmin = oAuth2Authentication.getPrincipal().equals("admin");

				String userId = String.valueOf(((JsonObject) oAuth2Authentication.getUserAuthentication().getCredentials()).get("user_id"));
				userIdPatientId = new IdDt(userId);

				OAuth2AccessToken oAuth2AccessToken = introspectingTokenService.readAccessToken(accessToken);
				ImmutableSet.Builder<Scope> builder = new ImmutableSet.Builder<Scope>();
				for (String scope : oAuth2AccessToken.getScope()) {
					builder.add(Scope.parse(scope));
				}
				scopes = builder.build();

				isJWTValid = true;
			} else {
				scopes = Sets.newConcurrentHashSet();
				isJWTValid = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Error parsing Bearer token (is it a valid JWT?)");
		}

		if (isJWTValid) {
			IAuthRuleBuilder ruleBuilder = new RuleBuilder();
			if (!userIsAdmin) {
				for (Scope scope : scopes) {
					if (scope.context != null && scope.context.equals(Scope.Context.PATIENT)) {
						if (scope.resource != null && scope.resource.equals("*")) {
							if (scope.read) {
								ruleBuilder = ruleBuilder.allow().read().allResources().inCompartment("Patient", userIdPatientId).andThen();
							}
							if (scope.write) {
								ruleBuilder = ruleBuilder.allow().write().allResources().inCompartment("Patient", userIdPatientId).andThen();
							}
						} else if (scope.resource != null && !scope.resource.isEmpty()) {
							if (scope.read) {
								ruleBuilder = ruleBuilder.allow().read().resourcesOfType(converter(scope.resource)).inCompartment("Patient", userIdPatientId).andThen();
							}
							if (scope.write) {
								ruleBuilder = ruleBuilder.allow().write().resourcesOfType(converter(scope.resource)).inCompartment("Patient", userIdPatientId).andThen();
							}
						}
					}
				}
				return ruleBuilder.denyAll().build();
			} else {
				for (Scope scope : scopes) {
					if (scope.context != null && scope.context.equals(Scope.Context.USER)) {
						if (scope.resource != null && scope.resource.equals("*")) {
							if (scope.read) {
								ruleBuilder = ruleBuilder.allow().read().allResources().withAnyId().andThen();
							} else if (scope.write) {
								ruleBuilder = ruleBuilder.allow().write().allResources().withAnyId().andThen();
							}
						} else if (scope.resource != null && !scope.resource.isEmpty()) {
							if (scope.read) {
								ruleBuilder = ruleBuilder.allow().read().resourcesOfType(converter(scope.resource)).withAnyId().andThen();
							} else if (scope.write) {
								ruleBuilder = ruleBuilder.allow().write().resourcesOfType(converter(scope.resource)).withAnyId().andThen();
							}
						}
					}
				}
				return ruleBuilder.denyAll().build();
			}
		} else {
			// Throw an HTTP 401
			throw new AuthenticationException("Bearer token not accepted");
		}
	}

	// TODO
	private Class<Patient> converter(String resourceType) {
		if (resourceType.equals("Patient")) {
			return Patient.class;
		}
		return null;
	}
}