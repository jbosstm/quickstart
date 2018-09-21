/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.cmt.controller;

import java.util.Date;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.jboss.as.quickstarts.cmt.jts.ejb.CustomerManagerEJB;
import org.jboss.logging.Logger;

@Named("customerManager")
@RequestScoped
public class CustomerManager {
	private Logger log = Logger.getLogger(CustomerManager.class);

	@EJB
	private CustomerManagerEJB customerManager;

	private static int numberOfCreatedCustomers = 0;
	private static String timeP;

	public String addCustomer() {
		try {
			Map<String,String> requestMap = FacesContext.getCurrentInstance()
					.getExternalContext().getRequestParameterMap();
			int invocationCount = 1;
			if (requestMap.get("name") != null) {
				invocationCount = Integer.parseInt((String) requestMap.get("name"));
			}
			
			log.infof("invocation count to be processed: %s, current time: %s", invocationCount, new Date());
			long time = System.currentTimeMillis();
			for (int i = 1; i <= invocationCount; i++) {
			    numberOfCreatedCustomers++;
				customerManager.createCustomer("customer number: " + numberOfCreatedCustomers + ", invocation count: " + i);
			}
			long timeNow = System.currentTimeMillis();

			timeP = "invocationCount " + invocationCount + " took: "
					+ (timeNow - time) + " which is " + ((timeNow - time)/ invocationCount) + " per invocation" ;
			log.info(timeP);

			return "customerAdded";
		} catch (Exception e) {
			log.warn("Problem: to invoke the remote EJB bean with the JTS context", e);
			return "customerDuplicate";
		}
	}

	public String getTime() {
		log.debug("static variable time: " + timeP);
		return timeP;
	}
}
