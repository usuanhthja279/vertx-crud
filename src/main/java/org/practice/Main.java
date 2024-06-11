package org.practice;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.extern.slf4j.Slf4j;
import org.practice.verticle.DataOperationVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(DataOperationVerticle.class);

    public static void main(String[] args) {
        logger.info("Main Class");
        VertxOptions vertxOptions = new VertxOptions()
                .setWorkerPoolSize(20)
                .setWarningExceptionTime(20000);
        Vertx vertx = Vertx.vertx(vertxOptions);
        vertx.deployVerticle(new DataOperationVerticle());
    }

}