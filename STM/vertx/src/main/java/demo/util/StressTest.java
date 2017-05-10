package demo.util;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class StressTest extends AbstractVerticle {
    private static ProgArgs opts;

    private static AtomicInteger passCnt = new AtomicInteger(0);
    private static AtomicInteger failCnt = new AtomicInteger(0);

    private static CountDownLatch targetCnt;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        opts = new ProgArgs(args);
        int parallelism = opts.getIntOption("parallelism", 20);
        int requests = opts.getIntOption("requests", 20);
        int target = parallelism * requests;

        targetCnt = new CountDownLatch(target);

        if (opts.getStringOption("help", null) != null) {
            System.out.printf("syntax: StressTest [parallelism=20] [requests=100] [port=8080] [url=/api/trip/Odeon/ABC]");
            return;
        }

        System.out.printf("Waiting for %d requests%n", target);

        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = new DeploymentOptions().setInstances(parallelism);
        long startTime = System.nanoTime();

        // deploy parallelism number of verticles that will concurrently update the same STM object
        vertx.deployVerticle(StressTest.class.getName(), options);

        targetCnt.await();

        long duration = (System.nanoTime() - startTime);
        System.out.printf("%d out of %d requests failed in %d ms%n", failCnt.get(), target, duration / 100000);

        vertx.close();
    }

    @Override
    public void start() throws Exception {
        HttpClient client = vertx.createHttpClient();
        String url = opts.getStringOption("url", "/api/trip/Odeon/ABC");
        int requests = opts.getIntOption("requests", 20);
        int port = opts.getIntOption("port",8080);
        boolean verbose = opts.getBooleanOption("verbose", false);

        IntStream.rangeClosed(1, requests).forEach(i -> invokeService(
                client, port, "localhost", url, verbose));
    }

    private void invokeService(HttpClient client, int port, String host, String url, boolean verbose) {
        client.post(port, host, url)
                .exceptionHandler(e -> {
                    incrFailCount(); System.out.printf("Theatre booking request failed: %s%n", e.getLocalizedMessage());
                })
                .handler(h -> {
                    incrPassCount(); if (verbose) System.out.printf("%s%n", h.toString());
                })
                .end();
    }

    private void incrFailCount() {
        failCnt.incrementAndGet();
        targetCnt.countDown();
    }

    private void incrPassCount() {
        passCnt.incrementAndGet();
        targetCnt.countDown();
    }
}
