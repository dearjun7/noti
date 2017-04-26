package com.hs.gms.srv.nti.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

    Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start() throws Exception {
        DeploymentOptions options = new DeploymentOptions();
        options.setConfig(config());
        options.setInstances(config().getInteger("vertx.instance.number"));

        vertx.deployVerticle("com.hs.gms.srv.nti.verticle.HttpServerVerticle", options, res -> {
            if(res.succeeded()) {
                logger.info("HttpServerVerticle Deployment id is: " + res.result());
            } else {
                logger.error("HttpServerVerticle Deployment failed!");
            }
        });
    }
}
