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