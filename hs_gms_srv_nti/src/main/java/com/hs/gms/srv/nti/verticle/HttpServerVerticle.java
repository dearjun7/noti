package com.hs.gms.srv.nti.verticle;

import com.hs.gms.srv.nti.handler.HttpEventHandler;
import com.hs.gms.srv.nti.handler.SockJSEventHandler;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

public class HttpServerVerticle extends AbstractVerticle {

    Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

    @Override
    public void start() throws Exception {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        JsonObject serverConf = config();

        try {
            SockJSEventHandler sockJSEventHandler = new SockJSEventHandler();
            HttpEventHandler httpHandler = new HttpEventHandler(serverConf);

            router.route().handler(httpHandler.getCorsHandler());

            // HTTP Context router setting
            router.route("/webNotification/*").handler(BodyHandler.create());

            // Test용 Context
            router.get("/testclient/*").handler(httpHandler.getEventBusTestHandler());

            router.get("/vertx-eventbus.js").handler(httpHandler.getVertxJavaScriptHandler());
            router.get("/sendMessage").handler(httpHandler.getSendMessageHandler());
            router.post("/webNotification").handler(httpHandler.getWebNotificationHandler(vertx));

            // Server START Information Log
            logger.info("========= HTTP Server Starting =========");
            logger.info(" heartbeatInterval : " + serverConf.getLong("heartbeatInterval"));
            logger.info(" insertJSESSIONID : " + serverConf.getBoolean("insertJSESSIONID"));
            logger.info(" maxBytesStreaming : " + serverConf.getInteger("maxBytesStreaming"));
            logger.info(" sessionTimeout : " + serverConf.getLong("sessionTimeout"));

            // SockJS 설정 setting
            SockJSHandlerOptions options = new SockJSHandlerOptions(serverConf);

            // sockJS 
            SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
            PermittedOptions permittedOptions = new PermittedOptions();
            BridgeOptions bridgeOptions = new BridgeOptions().addInboundPermitted(permittedOptions).addOutboundPermitted(permittedOptions);

            sockJSHandler.bridge(bridgeOptions, sockJSEventHandler.getBridgeEventHandler(vertx));

            router.route("/*").handler(sockJSHandler);

            // server 기동
            server.requestHandler(router::accept).listen(serverConf.getInteger("http.port"));

            logger.info("========= Notification Server is Started!!!! =========");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        logger.info("========= Notification Server is Stoped!!!! =========");
    }
}
