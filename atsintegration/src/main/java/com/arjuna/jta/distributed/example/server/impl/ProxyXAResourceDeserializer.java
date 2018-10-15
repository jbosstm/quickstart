/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.arjuna.jta.distributed.example.server.impl;

import java.io.IOException;
import java.io.ObjectInputStream;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.jta.recovery.SerializableXAResourceDeserializer;

/**
 * This is an additional recovery helper that allows a transport to provide a
 * deserializer for its ProxyXAResource. We need this as otherwise the
 * transaction manager would not be able to see the transports classes. Check
 * out the Javadocs on {@link SerializableXAResourceDeserializer}
 */
public class ProxyXAResourceDeserializer implements SerializableXAResourceDeserializer {

	@Override
	public boolean canDeserialze(String className) {
		if (className.equals(ProxyXAResource.class.getName())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public XAResource deserialze(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		return (XAResource) ois.readObject();
	}

}
