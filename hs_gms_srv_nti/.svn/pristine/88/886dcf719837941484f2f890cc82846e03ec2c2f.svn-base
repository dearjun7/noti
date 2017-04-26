package com.hs.gms.srv.nti;

import java.io.File;
import java.io.IOException;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Vert.x로 구현된 notification 서버 구동 Class - Stand Alone Mode
 * 
 * @author BH Jun
 */
public class NotificatorNonClusterModeStarter {

    private static Logger LOGGER = LoggerFactory.getLogger(NotificatorNonClusterModeStarter.class);

    private static final String DEFAULT_NOTI_CONF = "conf/config.json";
    private static final String LOCAL_NOTI_CONF = "src/main/resources/conf/config.json";

    /**
     * 서버 구동 메인 method
     * 
     * @param args
     * @throws IOException
     */
    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws IOException {
        String conf = null;

        File confFile = new File(DEFAULT_NOTI_CONF);

        if(confFile.exists()) {
            conf = DEFAULT_NOTI_CONF;

            LOGGER.info("LOAD CONFIG FILE - " + DEFAULT_NOTI_CONF);
        } else {
            conf = LOCAL_NOTI_CONF;
            LOGGER.info("LOAD CONFIG FILE - " + LOCAL_NOTI_CONF);
        }

        io.vertx.core.Starter.main(new String[]{"run", "com.hs.gms.srv.nti.verticle.MainVerticle", "-conf", conf});
    }
}
