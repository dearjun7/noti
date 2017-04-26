package com.hs.gms.srv.nti.handler;

import com.hs.gms.srv.nti.model.UserEventVO;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;

public class SockJSEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(SockJSEventHandler.class);

    private enum EventStatusType {
        SOCKET_CREATED {

            @Override
            public void loggingUserEventStatus(Vertx vertx, UserEventVO userEventVo) {
                logger.info(userEventVo.getUserIP() + "|New Socket CONNECTED.");
            }
        },
        SOCKET_CLOSED {

            @Override
            public void loggingUserEventStatus(Vertx vertx, UserEventVO userEventVo) {
                logger.info(userEventVo.getUserIP() + "|Socket CLOSED.");
            }
        },
        REGISTER {

            @Override
            public void loggingUserEventStatus(Vertx vertx, UserEventVO userEventVo) {
                logger.info(userEventVo.getUserIP() + "|user(" + userEventVo.getUserName() + ") JOIN.");
            }
        },
        UNREGISTER {

            @Override
            public void loggingUserEventStatus(Vertx vertx, UserEventVO userEventVo) {
                logger.info(userEventVo.getUserIP() + "|user(" + userEventVo.getUserName() + ") LEAVE.");
            }
        },
        SEND {

            @Override
            public void loggingUserEventStatus(Vertx vertx, UserEventVO userEventVo) {
                logger.info(userEventVo.getUserIP() + "|user(" + userEventVo.getUserName() + ") SENDED message(" + userEventVo.getUserMsg()
                        + ").");
            }
        },
        PUBLISH {

            @Override
            public void loggingUserEventStatus(Vertx vertx, UserEventVO userEventVo) {
                logger.info(userEventVo.getUserIP() + "|user(" + userEventVo.getUserName() + ") PUBLISHED message("
                        + userEventVo.getUserMsg() + ").");
            }
        },
        RECEIVE {

            @Override
            public void loggingUserEventStatus(Vertx vertx, UserEventVO userEventVo) {
                logger.info(userEventVo.getUserIP() + "|user(" + userEventVo.getUserName() + ") RECEIVED message("
                        + userEventVo.getUserMsg() + ").");
            }
        };

        public abstract void loggingUserEventStatus(Vertx vertx, UserEventVO userEventVo);
    }

    /**
     * SockJS Event 처리관련 handler
     * 
     * @return Handler<BridgeEvent>
     * @throws Exception
     */
    public Handler<BridgeEvent> getBridgeEventHandler(Vertx vertx) throws Exception {
        Handler<BridgeEvent> bridgeEvent = bridgeEventHandler -> {
            BridgeEventType bridgeEventType = bridgeEventHandler.type();
            UserEventVO userEventVo = new UserEventVO();

            userEventVo.setUserIP(bridgeEventHandler.socket().remoteAddress());
            userEventVo.setEventBus(vertx.eventBus());

            // User 최초 접속 /접속 종료 시, User 접속/종료 ID 확인
            if(bridgeEventType != BridgeEventType.SOCKET_CREATED && bridgeEventType != BridgeEventType.SOCKET_CLOSED) {
                userEventVo.setUserName(bridgeEventHandler.rawMessage().getString("address"));
            }

            // User 접속 시,User 리스트 갱신
            if(bridgeEventType == BridgeEventType.REGISTER || bridgeEventType == BridgeEventType.UNREGISTER) {

            }

            // User 메시지 전송 시, 전송되 Body 확인
            if(bridgeEventType == BridgeEventType.SEND || bridgeEventType == BridgeEventType.PUBLISH
                    || bridgeEventType == BridgeEventType.RECEIVE) {
                userEventVo.setUserMsg(bridgeEventHandler.rawMessage().getValue("body"));
            }

            EventStatusType.valueOf(bridgeEventType.toString()).loggingUserEventStatus(vertx, userEventVo);

            bridgeEventHandler.complete(true);
        };

        return bridgeEvent;
    }
}
