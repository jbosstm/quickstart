package org.jboss.narayana.blacktie.jatmibroker.client;

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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionFactory;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;

public class JavaClient {
    private static final Logger log = LogManager.getLogger(JavaClient.class);

    public static void main(String[] args) throws Exception {
        log.info("JavaClient");
        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        Connection connection = connectionFactory.getConnection();
        X_OCTET sbuf = (X_OCTET) connection.tpalloc("X_OCTET", null, 29);
        sbuf.setByteArray("THIS IS YOUR CLIENT SPEAKING".getBytes());
        log.info("Calling tpcall with input: %s" + new String(sbuf.getByteArray()));
        int cd = connection.tpacall("JAVASERV", sbuf, 0);
        Response retbuf = connection.tpgetrply(cd, 0);
        log.info("Called tpcall with length: %d output: %s" + retbuf.getBuffer().getLen() + " "
                + new String(((X_OCTET) retbuf.getBuffer()).getByteArray()));
        connection.close();
    }
}
