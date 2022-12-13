/*
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
package quickstart;

import io.undertow.Undertow;
import jakarta.ws.rs.core.Application;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;

public class JAXRSServer {
    private final UndertowJaxrsServer server;

    public JAXRSServer(String message, int port) {
        System.out.printf("starting undertow (%s)%n", message);
        server = new UndertowJaxrsServer();
        server.start(Undertow.builder().addHttpListener(port, "localhost"));
    }

    public void addDeployment(Application application, String contextRoot) {
        server.deploy(application, contextRoot);
    }

    public void stop() {
        server.stop();
    }
}
