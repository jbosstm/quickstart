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
import javax.inject.Inject;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.JtsTransactionImple;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionException;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionImpl;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.mdb.MDBBlacktieService;
import org.jboss.narayana.blacktie.quickstart.ejb.eg1.BTBean;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/BTR_TxEchoService") })
public class TxEchoServiceTestService extends MDBBlacktieService implements javax.jms.MessageListener {

    private static final Logger log = LogManager.getLogger(TxEchoServiceTestService.class);

    @Inject
    private BTBean btBean;

    public TxEchoServiceTestService() throws ConfigurationException {
        super("TxEchoServiceTestService");
    }

    public Response tpservice(TPSVCINFO svcinfo) throws ConnectionException, ConfigurationException {
        X_OCTET rcv = (X_OCTET) svcinfo.getBuffer();
        Connection connection = svcinfo.getConnection();
        String args = new String(rcv.getByteArray());
        String resp;

        log.info(args + " hasTransaction: " + JtsTransactionImple.hasTransaction());

        if (args.contains("tx=true")) {
            try {
                btBean.txNever();
                log.info("Error should have got a Not Supported Exception");
                resp = "Error should have got a Not Supported Exception";
            } catch (javax.ejb.EJBException e) {
                log.info("Success got Exception calling txNever: " + e);
                resp = args;
            }
        } else if (args.contains("tx=false")) {
            try {
                btBean.txMandatory();
                log.info("Error should have got an EJBTransactionRequiredException exception");
                resp = "Error should have got an EJBTransactionRequiredException exception";
            } catch (javax.ejb.EJBTransactionRequiredException e) {
                log.info("Success got EJBTransactionRequiredException");
                resp = args;
            }
        } else if (args.contains("tx=create")) {
            try {
                byte[] echo = args.getBytes();
                X_OCTET buffer = (X_OCTET) connection.tpalloc("X_OCTET", null);
                buffer.setByteArray(echo);

                log.info("Invoking TxCreateService...");
                Response response = connection.tpcall("TxCreateService", buffer, 0);
                X_OCTET rcvd = (X_OCTET) response.getBuffer();
                String responseData = new String(rcvd.getByteArray());
                log.info("TxCreateService response: " + responseData);

                // check that the remote service created a transaction
                TransactionImpl tx = TransactionImpl.current();
                if (tx != null) {
                    try {
                        tx.commit();
                    } catch (TransactionException e) {
                        args = "Service create a transaction but commit failed: " + e;
                        log.error(args, e);
                    }
                } else {
                    args = "Service should have propagated a new transaction back to caller";
                }

                resp = responseData; // should be the same as args
            } catch (ConnectionException e) {
                log.error("Caught a connection exception: " + e.getMessage(), e);
                resp = e.getMessage();
            } catch (ConfigurationException e) {
                log.error("Caught a configuration exception: " + e.getMessage(), e);
                resp = e.getMessage();
            }
        } else {
            resp = "unknown operation";
        }
        X_OCTET buffer = (X_OCTET) svcinfo.getConnection().tpalloc("X_OCTET", null);
        buffer.setByteArray(resp.getBytes());
        return new Response(Connection.TPSUCCESS, 0, buffer, 0);
    }
}
