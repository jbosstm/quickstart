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

import jakarta.ws.rs.core.UriBuilder;
import org.jboss.narayana.rest.integration.RecoveryManager;
import org.jboss.narayana.rest.integration.api.ParticipantsManagerFactory;
import org.jboss.resteasy.core.ResteasyDeploymentImpl;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class JAXRSServer {

    /*
     * use Netty instead of UndertowJaxrsServer because it provides
     * a simpler way to register the org.jboss.narayana.rest.integration.ParticipantResource class.
     * ParticipantResource proxies REST-AT transaction termination calls.
     */
    private static NettyJaxrsServer NETTY;

    public JAXRSServer(String host, int port) {
        System.out.printf("starting container (%s:%d)%n", host, port);
        initParticipantManager(host, port);
        start(TransactionAwareResource.class.getCanonicalName(), host, port);
    }

    private void initParticipantManager(String host, int port) {
        final URI baseUri= UriBuilder.fromUri("http://" + host + ':' + port + '/').build();
        // tell the ParticipantManager the base URI of resources managed by this server
        // which is needed when enlisting local resources with the remote REST-AT coordinator
        ParticipantsManagerFactory.getInstance().setBaseUrl(baseUri.toString());
    }

    public static void start(String resourceClass, String host, int port) {
        List<String> resourceClasses = new ArrayList<>();

        // register the REST-AT integration participant proxy
        resourceClasses.add("org.jboss.narayana.rest.integration.ParticipantResource");
        // register the local application resource
        resourceClasses.add(resourceClass);

        ResteasyDeployment resteasyDeployment = new ResteasyDeploymentImpl();

        resteasyDeployment.setResourceClasses(resourceClasses);

        NETTY = new NettyJaxrsServer();

        NETTY.setDeployment(resteasyDeployment);
        NETTY.setHostname(host);
        NETTY.setPort(port);
        NETTY.start();

        RecoveryManager.getInstance().registerDeserializer(TransactionAwareResource.APPLICATION_ID, new WorkDeserializer());
    }

    public void stop() {
        NETTY.stop();
    }
}
