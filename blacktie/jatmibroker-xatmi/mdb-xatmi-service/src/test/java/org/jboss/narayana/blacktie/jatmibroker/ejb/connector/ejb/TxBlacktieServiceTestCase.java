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
package org.jboss.narayana.blacktie.jatmibroker.ejb.connector.ejb;

import junit.framework.TestCase;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionException;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionImpl;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionFactory;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

public class TxBlacktieServiceTestCase extends TestCase {
    private static final Logger log = LogManager.getLogger(TxBlacktieServiceTestCase.class);

    public void testNeverWithTransaction() throws ConnectionException, TransactionException, ConfigurationException, NotFound, CannotProceed, InvalidName,
            org.omg.CORBA.ORBPackage.InvalidName, AdapterInactive {
        log.info("TxBlacktieServiceTestCase::test1");
        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        Connection connection = connectionFactory.getConnection();
        byte[] args = "test=test1,tx=true".getBytes();
        X_OCTET buffer = (X_OCTET) connection.tpalloc("X_OCTET", null);
        buffer.setByteArray(args);

        TransactionImpl transaction = new TransactionImpl(5000);
        Response response = connection.tpcall("TxEchoService", buffer, 0);
        String responseData = new String(((X_OCTET) response.getBuffer()).getByteArray());
        transaction.commit();
        assertEquals("test=test1,tx=true", responseData);
        connection.close();
    }

    public void testNeverWithoutTransaction() throws ConnectionException, ConfigurationException {
        log.info("TxBlacktieServiceTestCase::test2");
        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        Connection connection = connectionFactory.getConnection();
        byte[] args = "test=test2,tx=true".getBytes();
        X_OCTET buffer = (X_OCTET) connection.tpalloc("X_OCTET", null);
        buffer.setByteArray(args);

        Response response = connection.tpcall("TxEchoService", buffer, 0);
        String responseData = new String(((X_OCTET) response.getBuffer()).getByteArray());
        assertNotSame("test=test2,tx=true", responseData);
        connection.close();
    }

    public void testMandatoryWithoutTransaction() throws ConnectionException, ConfigurationException {
        log.info("TxBlacktieServiceTestCase::test3");
        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        Connection connection = connectionFactory.getConnection();
        byte[] args = "test=test3,tx=false".getBytes();
        X_OCTET buffer = (X_OCTET) connection.tpalloc("X_OCTET", null);
        buffer.setByteArray(args);

        Response response = connection.tpcall("TxEchoService", buffer, 0);
        String responseData = new String(((X_OCTET) response.getBuffer()).getByteArray());
        assertEquals("test=test3,tx=false", responseData);
        connection.close();
    }

    public void testMandatoryWithTransaction() throws ConnectionException, TransactionException, ConfigurationException, NotFound, CannotProceed, InvalidName,
            org.omg.CORBA.ORBPackage.InvalidName, AdapterInactive {
        log.info("TxBlacktieServiceTestCase::test4");
        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        Connection connection = connectionFactory.getConnection();
        byte[] args = "test=test4,tx=false".getBytes();
        X_OCTET buffer = (X_OCTET) connection.tpalloc("X_OCTET", null);
        buffer.setByteArray(args);

        TransactionImpl transaction = new TransactionImpl(5000);
        Response response = connection.tpcall("TxEchoService", buffer, 0);
        String responseData = new String(((X_OCTET) response.getBuffer()).getByteArray());
        transaction.commit();
        assertNotSame("test=test4,tx=false", responseData);
        connection.close();
    }

    /*
     * Test that the AS can create a transaction and propagate it too another blacktie service.
     */
    public void testCreateTransaction() throws ConnectionException, ConfigurationException {
        log.info("TxBlacktieServiceTestCase::test5");
        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        Connection connection = connectionFactory.getConnection();
        byte[] args = "test=test5,tx=create".getBytes();
        X_OCTET buffer = (X_OCTET) connection.tpalloc("X_OCTET", null);
        buffer.setByteArray(args);

        Response response = connection.tpcall("TxEchoService", buffer, 0);
        String responseData = new String(((X_OCTET) response.getBuffer()).getByteArray());
        assertEquals("test=test5,tx=create", responseData);
    }
}
