package demo.demo12;

import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import demo.domain.TheatreService;
import demo.domain.ServiceResult;

public abstract class TheatreVerticle extends TheatreVerticleImpl {
    private String SERVICE_NAME = "theatre";

    private TheatreService serviceClone;

    TheatreVerticle() {
    }

    abstract TheatreService getClone();

    @Override
    public void start(Future<Void> future) throws Exception {
        serviceClone = getClone();

        startServer(future, config().getInteger("port"));
    }

    // vertx plumbing and service handlers

    private void startServer(Future<Void> future, int listenerPort) {
        Router router = Router.router(vertx);

        getRoutes(router);

        // Create the HTTP server
        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(listenerPort,
                        result -> {
                            if (result.succeeded()) {
                                future.complete(); // tell the caller the server is ready
                            } else {
                                result.cause().printStackTrace(System.out);
                                future.fail(result.cause()); // tell the caller that server failed to start
                            }
                        }
                );

        assert router.getRoutes().size() > 0;

        String route1 = router.getRoutes().get(0).getPath();

        System.out.printf("%s: %s service listening on http://localhost:%d%s%n",
                Thread.currentThread().getName(), SERVICE_NAME, listenerPort, route1);
    }

    private void getRoutes(Router router) {
        router.get(String.format("/api/%s", SERVICE_NAME)).handler(this::getBookings);
        router.post(String.format("/api/%s/:seats", SERVICE_NAME)).handler(this::makeBooking);
    }

    private void getBookings(RoutingContext routingContext) {
        try {
            int bookings = getBookings(serviceClone);

            System.out.printf("%s: cnt: %d%n", SERVICE_NAME, serviceClone.getBookings());
            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(new ServiceResult(SERVICE_NAME, Thread.currentThread().getName(), bookings)));
        } catch (Exception e) {
            routingContext.response()
                    .setStatusCode(406)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(new JsonObject().put("Status", e.getMessage()).encode());
        }
    }

    private void makeBooking(RoutingContext routingContext) {
        try {
            int bookings = makeBooking(serviceClone);

            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(new ServiceResult(SERVICE_NAME, Thread.currentThread().getName(), bookings)));
        } catch (Exception e) {
            routingContext.response()
                    .setStatusCode(406)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(new JsonObject().put("Status", e.getMessage()).encode());
        }
    }
}
