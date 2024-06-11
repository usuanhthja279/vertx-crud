package org.practice;

import io.vertx.core.Vertx;
import org.practice.verticle.DataOperationVerticle;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new DataOperationVerticle());
    }
}