package com.hs.gms.srv.nti.handler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;

public class HttpEventHandler {

    Logger logger = LoggerFactory.getLogger(HttpEventHandler.class);

    private static final int HTTP_OK = 200;
    private static final int BAD_REQUEST = 400;

    private static final boolean CHUNKED = true;
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String HTML_CONTENT_TYPE = "text/html";

    private static final String RESULT_SUCCESS_MSG = "SUCCESS";
    private static final String RESULT_FAIL_MSG = "FAILURE";

    private static final String DEFAULT_VIEW_DIR = "WEB-INF/view";
    private static final String DEFAULT_JS_DIR = "WEB-INF/js";

    private static String accessControlAllowMethods = null;
    private static String accessControlMaxAge = null;
    private static String accessControlAllowHeaders = null;
    private static String accessControlAllowOrigin = null;

    private enum ResultCodeEnum {
        SUCCESS(0),
        FAILURE(1);

        private int resultCode;

        private ResultCodeEnum(int resultCode) {
            this.resultCode = resultCode;
        }

        public int getResultCode() {
            return resultCode;
        }
    }

    public HttpEventHandler(JsonObject serverConf) throws Exception {
        HttpEventHandler.accessControlAllowMethods = serverConf.getString("cors.access_control_allow_methods");
        HttpEventHandler.accessControlMaxAge = serverConf.getString("cors.access_control_max_age");
        HttpEventHandler.accessControlAllowHeaders = serverConf.getString("cors.access_control_allow_headers");
        HttpEventHandler.accessControlAllowOrigin = serverConf.getString("cors.access_control_allow_origin");
    }

    public CorsHandler getCorsHandler() throws Exception {
        CorsHandler cors = CorsHandler.create(accessControlAllowOrigin);
        String[] confMethodArr = accessControlAllowMethods.split(",");
        String[] confHeadersArr = accessControlAllowHeaders.split(",");

        Set<HttpMethod> allowMethods = new HashSet<HttpMethod>();
        Set<String> allowHeaders = new HashSet<String>();

        for(String tmpMethod : confMethodArr) {
            allowMethods.add(HttpMethod.valueOf(tmpMethod.trim().toUpperCase()));
        }

        for(String tmpHeader : confHeadersArr) {
            allowHeaders.add(tmpHeader.trim());
        }

        cors.allowedMethods(allowMethods);
        cors.allowedHeaders(allowHeaders);
        cors.maxAgeSeconds(Integer.parseInt(accessControlMaxAge == null ? "3600" : accessControlMaxAge));

        return cors;
    }

    /**
     * Event Bus 테스트 용 핸들러
     * 
     * @return Handler<RoutingContext>
     */
    public Handler<RoutingContext> getEventBusTestHandler() throws Exception {
        Handler<RoutingContext> routingHandler = routingContext -> {
            String viewName = routingContext.request().getParam("viewName");
            String htmlViewFile = "".equals(viewName) || viewName == null ? "eventBus.html" : viewName;

            this.httpSendResponseViewFile(routingContext, HTTP_OK, DEFAULT_VIEW_DIR + "/" + htmlViewFile);
        };

        return routingHandler;
    }

    /**
     * SendMessage 테스트 용 핸들러
     * 
     * @return Handler<RoutingContext>
     */
    public Handler<RoutingContext> getSendMessageHandler() throws Exception {
        Handler<RoutingContext> routingHandler = routingContext -> {
            this.httpSendResponseViewFile(routingContext, HTTP_OK, DEFAULT_VIEW_DIR + "/sendMessage.html");
        };

        return routingHandler;
    }

    /**
     * vertx-eventbus.js 클라이언트 전송 핸들러
     * 
     * @return Handler<RoutingContext>
     */
    public Handler<RoutingContext> getVertxJavaScriptHandler() throws Exception {
        Handler<RoutingContext> routingHandler = routingContext -> {
            this.httpSendResponseViewFile(routingContext, HTTP_OK, DEFAULT_JS_DIR + "/vertx-eventbus.js");
        };

        return routingHandler;
    }

    /**
     * notification 발송 기능 호출 핸들러
     * 
     * @param vertx
     * @throws UnsupportedEncodingException
     * @returnHandler<RoutingContext>
     */
    public Handler<RoutingContext> getWebNotificationHandler(Vertx vertx) throws Exception {
        Handler<RoutingContext> routingHandler = routingContext -> {
            JsonObject jsonBody = null;
            String body = null;

            logger.debug("routingContext.getBodyAsString() : " + routingContext.getBodyAsString());

            try {
                body = URLDecoder.decode(routingContext.getBodyAsString(), "UTF-8");
            } catch(UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if(!body.startsWith("{")) {
                jsonBody = this.oldRequestParamConvertToJsonObject(body);
            } else {
                jsonBody = routingContext.getBodyAsJson();
            }

            logger.debug("jsonBody = " + jsonBody + ", receive old type Params? : " + !body.startsWith("{"));

            this.sendResponseMessage(vertx, jsonBody, routingContext);
        };

        return routingHandler;
    }

    private JsonObject oldRequestParamConvertToJsonObject(String body) {
        Map<String, Object> reqMap = new HashMap<String, Object>();

        String[] dataPairArr = body.split("&");

        for(String dataPair : dataPairArr) {
            String[] tmpArr = dataPair.split("=");

            if(tmpArr.length > 1) {
                reqMap.put(tmpArr[0], tmpArr[1]);
            }
        }

        return new JsonObject(reqMap);
    }

    private void sendResponseMessage(Vertx vertx, JsonObject jsonBody, RoutingContext routingContext) {
        String id = jsonBody.getString("id");
        String data = null;
        Object tmpData = jsonBody.getValue("data");

        if(tmpData instanceof JsonObject) {
            data = ((JsonObject) tmpData).toString();
        } else if(tmpData instanceof JsonArray) {
            data = ((JsonArray) tmpData).toString();
        } else {
            data = (String) tmpData;
        }

        Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
        Map<String, Object> responseHeadMap = new HashMap<String, Object>();
        Map<String, Object> responseDataMap = new HashMap<String, Object>();

        logger.debug("id=" + id + ", data=" + data);

        String resResultCode = null;
        int resStatusCode = 0;

        if(id != null || data != null) {
            String[] userCodeArr = id.split(";");

            for(String userCode : userCodeArr) {
                EventBus eventBus = vertx.eventBus();

                eventBus.publish(userCode, data);
            }

            resResultCode = RESULT_SUCCESS_MSG;
            resStatusCode = HTTP_OK;

            responseDataMap.put("id", id);
            responseDataMap.put("data", data);
        } else {
            resResultCode = RESULT_FAIL_MSG;
            resStatusCode = BAD_REQUEST;

            responseDataMap.put("message", "must input at least One (id/data) Parameter");
        }

        responseHeadMap.put("resultCode", ResultCodeEnum.valueOf(resResultCode).getResultCode());
        responseHeadMap.put("resultMessage", ResultCodeEnum.valueOf(resResultCode));

        responseMap.put("responseHead", responseHeadMap);
        responseMap.put("responseData", responseDataMap);

        JsonObject json = new JsonObject(responseMap);

        logger.debug("response Map = " + json);

        this.httpSendResponseMessage(routingContext, resStatusCode, json.toString());
    }

    private void httpSendResponseMessage(RoutingContext routingContext, int statusCode, String resultMsg) {
        HttpServerResponse res = routingContext.response();

        this.makeHttpResposeHeader(res, JSON_CONTENT_TYPE);
        res.setStatusCode(statusCode);
        res.write(resultMsg);

        routingContext.response().end();
    }

    private void httpSendResponseViewFile(RoutingContext routingContext, int statusCode, String fileName) {
        HttpServerResponse res = routingContext.response();

        this.makeHttpResposeHeader(res, HTML_CONTENT_TYPE);
        res.setStatusCode(statusCode);
        res.sendFile(fileName);
    }

    private void makeHttpResposeHeader(HttpServerResponse res, String contentType) {
        res.setChunked(CHUNKED);
        res.putHeader("Content-Type", contentType);
    }
}
