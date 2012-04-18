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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.mdb.MDBBlacktieService;

@javax.ejb.TransactionAttribute(javax.ejb.TransactionAttributeType.NEVER)
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/BTR_TxCreateService") })
public class TxCreateServiceTestService extends MDBBlacktieService implements javax.jms.MessageListener {
    private static final Logger log = LogManager.getLogger(TxCreateServiceTestService.class);

    public TxCreateServiceTestService() throws ConfigurationException {
        super("TxCreateServiceTestService");
    }

    public Response tpservice(TPSVCINFO svcinfo) {
        try {
            Context context = new InitialContext();
            TransactionManager tm = (TransactionManager) context.lookup("java:/TransactionManager");
            tm.begin();
        } catch (Exception e) {
            log.error("Caught an exception", e);
            return new Response(Connection.TPFAIL, 0, null, 0);
        }

        return new Response(Connection.TPSUCCESS, 0, svcinfo.getBuffer(), 0);
    }
}
