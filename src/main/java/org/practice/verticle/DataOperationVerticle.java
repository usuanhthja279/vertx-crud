package org.practice.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class DataOperationVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(DataOperationVerticle.class);
    private Vertx vertx;

    public void init() {
        VertxOptions vertxOptions = new VertxOptions()
                .setWorkerPoolSize(20)
                .setWarningExceptionTime(20000);

        this.vertx = Vertx.vertx(vertxOptions);
    }
    @Override
    public void start(Promise<Void> promise) {
        String logFactory = System.getProperty("org.vertx.logger-delegate-factory-class-name");
        if (logFactory == null) {
            System.setProperty("org.vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
        }
        this.init();
        logger.info("Vertx Instance started");
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/api/test").handler(this::getData);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8990, result -> {
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
                .end(Json.encode(new JsonObject().put("name", "Sushant")));
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        vertx.close();
    }
}
