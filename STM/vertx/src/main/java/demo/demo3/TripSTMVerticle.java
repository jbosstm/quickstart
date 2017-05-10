package demo.demo3;

import demo.domain.TaxiService;
import demo.domain.TaxiServiceImpl;
import demo.domain.TheatreService;
import demo.domain.TheatreServiceImpl;
import demo.domain.ServiceResult;
import demo.util.ProgArgs;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.jboss.stm.Container;

public class TripSTMVerticle extends TripVerticleImpl {
    private String SERVICE_NAME = "trip";

    private static Container<TheatreService> theatreContainer;
    private static Container<TaxiService> taxiContainer;

    private static TaxiService taxiService;
    private static TaxiService altTaxiService;
    private static TheatreService theatreService;

    private TheatreService theatreServiceClone;
    private TaxiService taxiServiceClone;
    private TaxiService altTaxiServiceClone;

    private static int tripServicePort = 8080;

    public static void main(String[] args) {
        ProgArgs options = new ProgArgs(args);

        tripServicePort = options.getIntOption("trip.port", tripServicePort);

        theatreContainer = new Container<>(Container.TYPE.RECOVERABLE, Container.MODEL.EXCLUSIVE);
        taxiContainer = new Container<>(Container.TYPE.RECOVERABLE, Container.MODEL.EXCLUSIVE);

        theatreService = theatreContainer.create(new TheatreServiceImpl());
        taxiService = taxiContainer.create(new TaxiServiceImpl());
        altTaxiService = taxiContainer.create(new TaxiServiceImpl());

        initSTMMemory(theatreService);
        initSTMMemory(taxiService);
        initSTMMemory(altTaxiService);

        Vertx vertx = Vertx.vertx();

        DeploymentOptions opts = new DeploymentOptions()
                .setInstances(options.getIntOption("parallelism", 10))
                .setConfig(new JsonObject().put("trip.port", tripServicePort));

        vertx.deployVerticle(TripSTMVerticle.class.getName(), opts);
    }

    @Override
    public void start(Future<Void> future) throws Exception {
        theatreServiceClone = theatreContainer.clone(new TheatreServiceImpl(), theatreService);
        taxiServiceClone = taxiContainer.clone(new TaxiServiceImpl(), taxiService);
        altTaxiServiceClone = taxiContainer.clone(new TaxiServiceImpl(), taxiService);

        startServer(future, config().getInteger("trip.port"));
    }

    // vertx plumbing and service handlers

    private void getRoutes(Router router) {
        router.post(String.format("/api/%s/:name/:taxi", SERVICE_NAME)).handler(this::bookTrip);

        router.get("/api/theatre").handler(this::listTheatreBookings);
        router.get("/api/taxi").handler(this::listTaxiBookings);

        router.post("/api/theatre/:name").handler(this::bookTheatre);
        router.post("/api/taxi/:name").handler(this::bookTaxi);
    }

    private void startServer(Future<Void> future, int listenerPort) {
        Router router = Router.router(vertx);

        getRoutes(router);

        // Create the HTTP server and pass the "accept" method to the request handler.
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

    private void listTaxiBookings(RoutingContext routingContext) {
        try {
            int bookings = getBookings(taxiServiceClone);

            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(new ServiceResult("taxi", Thread.currentThread().getName(), bookings)));
        } catch (Exception e) {
            routingContext.response()
                    .setStatusCode(406)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(new JsonObject().put("Status", e.getMessage()).encode());
        }
    }

    private void listTheatreBookings(RoutingContext routingContext) {
        try {
            int bookings = getBookings(theatreServiceClone);

            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(new ServiceResult("theatre", Thread.currentThread().getName(), bookings)));
        } catch (Exception e) {
            routingContext.response()
                    .setStatusCode(406)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(new JsonObject().put("Status", e.getMessage()).encode());
        }
    }

    private void bookTrip(RoutingContext routingContext) {
        try {
            ServiceResult result = bookTrip(SERVICE_NAME, theatreServiceClone, taxiServiceClone, altTaxiServiceClone);

            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(result));
        } catch (Exception e) {
            routingContext.response()
                    .setStatusCode(406)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(new JsonObject().put("Status", e.getMessage()).encode());
        }
    }

    private void bookTheatre(RoutingContext routingContext) {
        try {
            int bookings = bookShow(theatreServiceClone);

            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(new ServiceResult("theatre", Thread.currentThread().getName(), bookings)));
        } catch (Exception e) {
            routingContext.response()
                    .setStatusCode(406)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(new JsonObject().put("Status", e.getMessage()).encode());
        }
    }

    private void bookTaxi(RoutingContext routingContext) {
        try {
            int bookings = bookTaxi(taxiServiceClone);

            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(new ServiceResult("taxi", Thread.currentThread().getName(), bookings)));
        } catch (Exception e) {
            routingContext.response()
                    .setStatusCode(406)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(new JsonObject().put("Status", e.getMessage()).encode());
        }
    }
}
