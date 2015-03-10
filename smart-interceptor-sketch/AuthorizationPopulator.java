package org.smartplatforms;

import javax.servlet.http.HttpServletRequest;

public interface AuthorizationPopulator {
	public Authorization fromRequest(HttpServletRequest theServletRequest);
}
