package org.smartplatforms;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.DateUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class AuthorizationPopulatorByIntrospection implements AuthorizationPopulator {

	@Override
	public Authorization fromRequest(HttpServletRequest theServletRequest) {
		
		final Set<Scope> scopes = new ImmutableSet.Builder<Scope>()
				.add(Scope.parse("user/*.read"))
				.add(Scope.parse("user/Observation.write"))
				.build();
		
		final Map<String,String> launchContext = new ImmutableMap.Builder<String,String>()
				.put("patient", "test-patient")
				.put("encounter", "123")
				.put("intent", "reconcile-medications")
				.build();
				
		return new Authorization(
				scopes,
				"jmandel",
				DateUtils.addHours(new Date(), 1),
				launchContext);
	}

}
