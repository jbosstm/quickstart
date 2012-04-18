/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
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
package org.jboss.narayana.blacktie.quickstart.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.mdb.MDBBlacktieService;

@javax.ejb.TransactionAttribute(javax.ejb.TransactionAttributeType.NOT_SUPPORTED)
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/BTR_EchoService") })
public class EchoServiceTestService extends MDBBlacktieService implements javax.jms.MessageListener {

    public EchoServiceTestService() throws ConfigurationException {
        super("EchoServiceTestService");
    }

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException {
        X_OCTET rcvd = (X_OCTET) svcinfo.getBuffer();
        X_OCTET buffer = (X_OCTET) svcinfo.getConnection().tpalloc("X_OCTET", null);
        buffer.setByteArray(rcvd.getByteArray());
        return new Response(Connection.TPSUCCESS, 0, buffer, 0);
    }
}
