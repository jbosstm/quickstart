package demo.demo12;

import com.arjuna.ats.arjuna.common.Uid;
import demo.domain.TheatreServiceImpl;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import org.jboss.stm.Container;

import demo.domain.TheatreService;

/**
 * Demonstrates how to use persistent STM objects with verticles
 */
public class PersistentSharedTheatreVerticle extends TheatreVerticle {
    private static int port = 8080;
    private static String uid;
    private static Container<TheatreService> container;

    public static void main(String[] args) {
        container = new Container<>(Container.TYPE.PERSISTENT, Container.MODEL.SHARED);

        uid = args.length != 0 ? args[0] : System.getProperty("uid");

        if (uid != null) {
            port = 8082;
        } else {
            TheatreService service = container.create(new TheatreServiceImpl());
            uid = container.getIdentifier(service).toString();
            initSTMMemory(service);

            System.out.printf("Theatre STM uid: %s%n", uid);
        }

        DeploymentOptions opts = new DeploymentOptions().
                setInstances(10).
                setConfig(new JsonObject().put("port", port));

        Vertx.vertx().deployVerticle(PersistentSharedTheatreVerticle.class.getName(), opts);

    }

    TheatreService getClone() {
        return container.clone(new TheatreServiceImpl(), new Uid(uid));
    }
}
