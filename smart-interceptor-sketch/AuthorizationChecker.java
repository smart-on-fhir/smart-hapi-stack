package org.smartplatforms;

import org.hl7.fhir.instance.model.IBaseResource;


public interface AuthorizationChecker {
	public boolean resourceIsReadable(IBaseResource resource, Authorization authorization);
	public boolean resourceIsWriteable(IBaseResource resource, Authorization authorization);
}
