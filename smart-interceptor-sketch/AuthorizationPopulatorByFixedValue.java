package org.smartplatforms;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.DateUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class AuthorizationPopulatorByFixedValue implements
		AuthorizationPopulator {

	private String username;

	public void setUsername(String u) {
		username = u;
	}

	public String getUsername() {
		return username;
	}

	private Set<Scope> scopes;

	public void setScopes(Set<String> inScopes) {
		Builder<Scope> b = new ImmutableSet.Builder<Scope>();
		for (String sWire : inScopes) {
			Scope s = Scope.parse(sWire);
			if (s.valid) {
				b.add(s);
			}
		}
		scopes = b.build();
	}

	public Set<Scope> getScopes() {
		return scopes;
	}

	private Map<String, String> launchContext;

	public void setLaunchContext(Map<String, String> inContext) {
		ImmutableMap.Builder<String, String> b = new ImmutableMap.Builder<String, String>();
		b.putAll(inContext);
		launchContext = b.build();
	}

	public Map<String, String> getLaunchContext() {
		return launchContext;
	}

	@Override
	public Authorization fromRequest(HttpServletRequest theServletRequest) {
		return new Authorization(scopes, username, DateUtils.addHours(new Date(), 1), launchContext);
	}

}
