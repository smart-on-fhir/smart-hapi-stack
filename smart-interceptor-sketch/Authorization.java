package org.smartplatforms;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class Authorization {
	
	public final Set<Scope> scopes;
	public final String user;
	public final Date expires;
	public final Map<String,String> launchContext;
	
	public Authorization (Set<Scope> scopes, String user, Date expires, Map<String,String> launchContext) {
		this.scopes = scopes;
		this.user = user;
		this.expires = expires;
		this.launchContext = launchContext;
	}

}