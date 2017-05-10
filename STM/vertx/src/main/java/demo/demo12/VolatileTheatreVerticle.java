package demo.demo12;

import demo.domain.TheatreServiceImpl;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import org.jboss.stm.Container;

import demo.domain.TheatreService;

/**
 * Demonstrates how to use volatile STM objects with verticles
 */
public class VolatileTheatreVerticle extends TheatreVerticle {
    private static int port = 8080;
    private static TheatreService service;
    private static Container<TheatreService> container;

    public static void main(String[] args) {
        container = new Container<>();

        service = container.create(new TheatreServiceImpl());
        initSTMMemory(service);

        DeploymentOptions opts = new DeploymentOptions().
                setInstances(10).
                setConfig(new JsonObject().put("port", port));

        Vertx.vertx().deployVerticle(VolatileTheatreVerticle.class.getName(), opts);
    }

    TheatreService getClone() {
        return container.clone(new TheatreServiceImpl(), service);
    }
}
