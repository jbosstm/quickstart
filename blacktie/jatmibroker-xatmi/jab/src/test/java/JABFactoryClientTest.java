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
import org.jboss.narayana.blacktie.jatmibroker.jab.JABException;
import org.jboss.narayana.blacktie.jatmibroker.jab.factory.JABBuffer;
import org.jboss.narayana.blacktie.jatmibroker.jab.factory.JABConnection;
import org.jboss.narayana.blacktie.jatmibroker.jab.factory.JABConnectionFactory;
import org.jboss.narayana.blacktie.jatmibroker.jab.factory.JABResponse;
import org.jboss.narayana.blacktie.jatmibroker.jab.factory.Transaction;

public class JABFactoryClientTest {
    private static final Logger log = LogManager.getLogger(JABFactoryClientTest.class);

    public static void main(String[] args) throws Exception {
        log.info("JABClient");
        if (args.length != 1) {
            log.error("java JABFactoryClient message");
            return;
        }
        String message = args[0];
        try {
            JABConnectionFactory jcf = new JABConnectionFactory("JABFactoryClientTest");
            JABConnection c = jcf.getConnection("connection");
            Transaction t = c.beginTransaction(-1);
            JABBuffer b = new JABBuffer();
            b.setValue("X_OCTET", message.getBytes());
            log.info("Calling call with input: " + message);
            JABResponse call = c.call("FOOAPP", b, t, "X_OCTET", null);
            log.info("Called call with output: " + call.getValue("X_OCTET"));
            t.commit();
            jcf.closeConnection("connection");
        } catch (JABException e) {
            log.error("JAB error: " + e.getMessage(), e);
        }
    }
}
