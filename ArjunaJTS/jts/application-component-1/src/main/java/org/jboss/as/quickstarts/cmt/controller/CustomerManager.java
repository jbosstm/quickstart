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
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.jboss.as.quickstarts.cmt.jts.ejb.CustomerManagerEJB;

@Named("customerManager")
@RequestScoped
public class CustomerManager {
	private Logger logger = Logger.getLogger(CustomerManager.class.getName());

	@EJB
	private CustomerManagerEJB customerManager;

	private static String timeP;

	public String addCustomer() {
		try {
			Map requestMap = FacesContext.getCurrentInstance()
					.getExternalContext().getRequestParameterMap();
			int invocationCount = 1;
			if (requestMap.get("name") != null) {
				invocationCount = Integer.parseInt((String) requestMap
						.get("name"));
			}
			System.out.println("invocation count: " + invocationCount);
			long time = System.currentTimeMillis();
			System.out.println(new Date());
			for (int i = 0; i < invocationCount; i++) {
				customerManager.createCustomer(i + "");
			}
			long timeNow = System.currentTimeMillis();
			timeP = "invocationCount " + invocationCount + " took: "
					+ (timeNow - time) + " which is " + (timeNow - time)
					/ invocationCount;
			System.out.println(timeP);

			return "customerAdded";
		} catch (Exception e) {
			logger.warning("Problem: " + e.getMessage());
			e.printStackTrace();
			// Transaction will be marked rollback only anyway utx.rollback();
			return "customerDuplicate";
		}
	}

	public String getTime() {
		System.out.println("WARNING: THIS IS FROM A STATIC: " + timeP);
		return timeP;
	}
}
