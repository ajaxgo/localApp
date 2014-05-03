package log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName LogForLogBack.java
 * @version 1.0
 * @Description
 * @author tsw
 * @date 2013-12-24 下午9:58:29
 * @Copyright 中国气象信息中心
 */

public class LogForLogBack {

    /**
     * @param args
     */
    public static void main (String[] args) {
        Logger logger = LoggerFactory.getLogger(LogForLogBack.class);
        logger.debug("output logs");
        logger.info("output logs");
        logger.error("output logs");

    }

}
