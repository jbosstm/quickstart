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
package org.jboss.narayana.blacktie.quickstarts.integration1.xatmi_adapter;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_COMMON;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.mdb.MDBBlacktieService;
import org.jboss.narayana.blacktie.quickstarts.integration1.ejb.CreditRemote;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/BTR_CREDITEXAMPLE") })
@javax.ejb.TransactionAttribute(javax.ejb.TransactionAttributeType.NEVER)
public class CreditAdapterService extends MDBBlacktieService implements javax.jms.MessageListener {
    private static final Logger log = LogManager.getLogger(CreditAdapterService.class);

    public CreditAdapterService() throws ConfigurationException {
        super("CreditAdapterService");
    }

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException {
        X_COMMON rcv = (X_COMMON) svcinfo.getBuffer();
        long acct_no = rcv.getLong("acct_no");
        short amount = rcv.getShort("amount");

        String resp = "NAMINGERROR";
        try {
            Context ctx = new InitialContext();
            CreditRemote bean = (CreditRemote) ctx
                    .lookup("java:global/blacktie-quickstarts-integration1-ejb-ear-5.0.0.Final-SNAPSHOT/blacktie-quickstarts-integration1-ejb-5.0.0.Final-SNAPSHOT/CreditBean!org.jboss.narayana.blacktie.quickstarts.integration1.ejb.CreditRemote");
            log.debug("resolved CreditBean");
            resp = bean.credit(acct_no, amount);
        } catch (NamingException e) {
            log.error("Got a naming error: " + e.getMessage(), e);
        }
        log.trace("Returning: " + resp);

        X_OCTET buffer = (X_OCTET) svcinfo.getConnection().tpalloc("X_OCTET", null);
        buffer.setByteArray(resp.getBytes());
        return new Response(Connection.TPSUCCESS, 0, buffer, 0);
    }
}
