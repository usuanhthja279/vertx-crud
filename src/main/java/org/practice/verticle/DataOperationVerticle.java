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
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
        router.route().handler(this::checkAuthToken);
        router.route().handler(this::generateCorrelationId);
        router.route().handler(this::loggingInterceptor);
        router.route().handler(this::addCommonHeader);

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

    private void checkAuthToken(RoutingContext routingContext) {
        MDC.clear();
        String authHeader = routingContext.request().getHeader("Authorization");
        if (authHeader == null || !authHeader.equals("Bearer SushantToken")) {
            logger.error("Error: Please Provide Auth Token in Headers");
            routingContext.response().setStatusCode(401).end("Unauthorized");
        } else {
            routingContext.next();
        }
    }

    private void generateCorrelationId(RoutingContext context) {
        String correlationId = UUID.randomUUID().toString();
        context.put("correlationId", correlationId);
        context.next();
    }

    private void loggingInterceptor(RoutingContext routingContext) {
        MDC.put("correlationID", routingContext.get("correlationId"));
        routingContext.next();
    }

    private void addCommonHeader(RoutingContext routingContext) {
        routingContext.response().putHeader("personal-correlation-id", MDC.get("correlationID"));
        routingContext.next();
    }

    private void getData(RoutingContext routingContext) {
        logger.info("GET Responses: {}", inMemoryData);
        routingContext.response()
                .putHeader("content-type", "application/json")
                .end(Json.encode(inMemoryData));
    }

    private void postData(RoutingContext routingContext) {
        logger.info("POST Data");
        JsonObject request = routingContext.body().asJsonObject();
        inMemoryData.put("postRequest" + i.getAndIncrement(), request);
        routingContext.response()
                .putHeader("content-type", "application/json")
                .end(Json.encode("Request Successfully posted"));
    }

    private void deleteData(RoutingContext routingContext) {
        logger.info("DELETE Data");
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
