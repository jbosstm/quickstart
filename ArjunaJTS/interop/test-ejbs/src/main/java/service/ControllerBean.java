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

import org.omg.CORBA.ORB;
import service.remote.ISession;
import service.remote.ISessionHome;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.Properties;

@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class ControllerBean {

    private boolean isWF;

    @PostConstruct
    public void init() {
        isWF = System.getProperty("jboss.node.name") != null;
        System.setProperty("com.sun.CORBA.ORBUseDynamicStub", "true");
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String getNext(boolean local, String arg) {
        return getNext(local, null, 0, arg);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String getNext(boolean local, String as, int jndiPort, String failureType) {
        try {
            boolean enlistAfter = failureType != null && failureType.contains("after");

            if (!enlistAfter)
                TxnHelper.addResources(isWF);

            String res = "";
            for (ISession ejb : getServiceEJBs(local, as, jndiPort))
                res = ejb.getNext(failureType);

            if (enlistAfter)
                TxnHelper.addResources(isWF);

            return res;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private ISession[] getServiceEJBs(boolean local, String as, int jndiPort) {
        if (!local && !isWF && as != null && as.contains("gf") && as.contains("wf")) {
            ISession[] ejbs = new ISession[2];
            ejbs[0] = getServiceEJB(false, "gf", jndiPort);
            ejbs[1] = getServiceEJB(false, "wf", jndiPort);

            return ejbs;
        } else {
            return new ISession[] {getServiceEJB(local, as, jndiPort)};
        }
    }

    private int validateJndiPort(int jndiPort, int defaultPort) {
        return (jndiPort <= 0 ? defaultPort : jndiPort);
    }

    private ISession getServiceEJB(boolean local, String as, int jndiPort) {
        // "java:global/[<application-name>]/<module-name>/<bean-name>!<fully-qualified-bean-interface-name>"
        String name = "java:global/ejbtest/SessionBean!service.remote.ISessionHome";
        Properties env = new Properties();

        if (!local) {
            if (isWF) {
                // running on a WildFly server
                if ("wf".equals(as)) {
                    jndiPort = validateJndiPort(jndiPort, 3628); // corba name service port for wildfly server2

                    name = "ejbtest/SessionBean";
                } else {
                    jndiPort = validateJndiPort(jndiPort, 7001); // corba name service port for glassfish domain1
                }

                env.put(Context.PROVIDER_URL, String.format("corbaloc::localhost:%d/NameService", jndiPort));
                env.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.iiop.openjdk.naming.jndi.CNCtxFactory");

            } else {
                // running on a glassfish server
                if ("gf".equals(as)) { // lookup an ejb running on a glassfish server

                    jndiPort = validateJndiPort(jndiPort, 3700); // corba name service port for glassfish domain2

                    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.enterprise.naming.SerialInitContextFactory");
                    env.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
                    env.setProperty("org.omg.CORBA.ORBInitialPort", String.valueOf(jndiPort));

                } else {  // lookup an ejb running on a wildfly server
                    name = "ejbtest/SessionBean";

                    jndiPort = validateJndiPort(jndiPort, 3528); // default corba name service port for wildfly

                    env.put(Context.PROVIDER_URL, String.format("iiop://localhost:%d", jndiPort));
                    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.cosnaming.CNCtxFactory");

                    try {
                        ORB orb = (ORB) new InitialContext().lookup("java:comp/ORB");
                        env.put("java.naming.corba.orb", orb);
                    } catch (NamingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        try {
            System.out.printf("looking up %s via port %d%n", name, jndiPort);
            InitialContext ctx = new InitialContext(env);

            Object oRef = ctx.lookup(name);
            ISessionHome home = (ISessionHome) oRef; //PortableRemoteObject.narrow(oRef, ISessionHome.class);

            return home.create();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
