/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package service;

import service.remote.ISessionHome;

import javax.annotation.PostConstruct;
import javax.ejb.RemoteHome;

import javax.ejb.Stateless;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import java.util.concurrent.atomic.AtomicInteger;

@Stateless
@RemoteHome(ISessionHome.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class SessionBean {
	static AtomicInteger counter = new AtomicInteger(0);
	boolean isWF;

	@PostConstruct
	public void init() {
		isWF = System.getProperty("jboss.node.name") != null;

		counter.set(isWF ? 8000 : 7000);
	}

	@TransactionAttribute(TransactionAttributeType.MANDATORY)
	public String getNext() {
		return getNext(null);
	}

	@TransactionAttribute(TransactionAttributeType.MANDATORY)
	public String getNext(String failureType) {
		try {
			TxnHelper.addResources(isWF, failureType);

			System.out.printf("%s returning next counter%n", this);
			return String.valueOf(counter.getAndIncrement());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
