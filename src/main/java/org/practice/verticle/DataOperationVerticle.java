package org.practice.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
//import io.vertx.core.impl.logging.Logger;
//import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DataOperationVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(DataOperationVerticle.class);
    Map<String, JsonObject> inMemoryData = new HashMap<>();
    AtomicInteger i = new AtomicInteger(0);

    @Override
    public void start(Promise<Void> promise) {
        logger.info("Vertx Instance started");
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/api/test").handler(this::getData);
        router.post("/api/post/test").handler(this::postData);
        router.delete("/api/delete/test").handler(this::deleteData);

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        logger.info("System running on PORT: {}", port);
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, result -> {
                    if (result.succeeded()) {
                        promise.complete();
                    } else {
                        promise.fail(result.cause().getMessage());
                    }
                });
        logger.info("Verticle Created");
    }

    public void getData(RoutingContext routingContext) {
        logger.info("Responses");
        routingContext.response()
                .putHeader("personal", "app")
                .putHeader("content-type", "application/json")
                .end(Json.encode(inMemoryData));
    }

    public void postData(RoutingContext routingContext) {
        logger.info("Post Data");
        JsonObject request = routingContext.body().asJsonObject();
        inMemoryData.put("postRequest" + i.getAndIncrement(), request);
        routingContext.response()
                .putHeader("content-type", "application/json")
                .end(Json.encode("Request Successfully posted"));
    }

    public void deleteData(RoutingContext routingContext) {
        logger.info("Delete Data");
        String request = routingContext.body().asString();
        if (inMemoryData.containsKey(request)) {
            inMemoryData.remove(request);
            logger.info("Data deleted");
        } else {
            logger.warn("Please enter correct Request id to be deleted");
        }
        routingContext.response()
                .putHeader("content-type", "application/text")
                .end("Data deleted: " + request);
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        vertx.close();
    }
}
