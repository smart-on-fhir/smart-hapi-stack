package org.smartplatforms;

import org.hl7.fhir.instance.model.IBaseResource;

import com.google.common.collect.ImmutableSet;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.util.FhirTerser;

public class AuthorizationCheckerSimple implements AuthorizationChecker {

	private FhirContext fhirContext;
	private FhirTerser fhirTerser;

	public FhirContext getFhirContext() {
		return fhirContext;
	}

	public void setFhirContext(FhirContext fhirContext) {
		this.fhirContext = fhirContext;
		this.fhirTerser = fhirContext.newTerser();
	}

	@Override
	public boolean resourceIsReadable(IBaseResource resource,
			Authorization authorization) {

		String resourceType = resource.getClass().getSimpleName();

		if (!resourceTypeIsReadable(resourceType, authorization)) {
			return false;
		}

		if (readIsRestrictedByPatient(resourceType, authorization)) {
			for (ResourceReferenceDt ref : fhirTerser.getAllPopulatedChildElementsOfType(resource, ResourceReferenceDt.class)) {
				if (differentPatient(ref, authorization)) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean differentPatient(ResourceReferenceDt ref, Authorization authorization) {
		IdDt refId = ref.getReference();
		if (!refId.getResourceType().equals("Patient")){
			return false;
		}
		
		if (refId.getIdPart().equals(authorization.launchContext.get("patient"))){
			return false;
		}
		
		return true;
	}

	private boolean readIsRestrictedByPatient(String resourceType,
			Authorization authorization) {
		return false;
	}

	private boolean resourceTypeIsReadable(String resource,
			Authorization authorization) {
		return false;
	}

	@Override
	public boolean resourceIsWriteable(IBaseResource resource,
			Authorization authorization) {
		// TODO Auto-generated method stub
		return false;
	}

}
