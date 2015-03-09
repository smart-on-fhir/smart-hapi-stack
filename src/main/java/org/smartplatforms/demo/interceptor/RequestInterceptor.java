package org.smartplatforms.demo.interceptor;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.smartplatforms.demo.model.SmartUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;

public class RequestInterceptor extends InterceptorAdapter {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(RequestInterceptor.class);

	@PersistenceContext(name = "FHIR_UT", type = PersistenceContextType.TRANSACTION, unitName = "FHIR_UT")
	private EntityManager myEntityManager;

	@Autowired
	private PlatformTransactionManager myPlatformTransactionManager;

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public boolean incomingRequestPreProcessed(HttpServletRequest theRequest, HttpServletResponse theResponse) {

		String user = "steve"; // let's pretend the user is called Steve

		TypedQuery<SmartUser> q = myEntityManager.createQuery("SELECT u FROM SmartUser u WHERE u.myUsername = :name", SmartUser.class);
		q.setParameter("name", user);

		try {
			SmartUser result = q.getSingleResult();
			ourLog.info("Found user: {}", result);
		} catch (NoResultException e) {
			// This will always trigger for now, because there are no users!
			ourLog.info("Did not find user: {}", user);
		}
		return true;
	}

}
