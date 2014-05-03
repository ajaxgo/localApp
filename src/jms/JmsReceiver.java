/* 项目名称：新一代天气雷达信息共享平台 （CIMISS） 类 名: JmsReceiver.java 版本信息：V1.0 日 期：2012-1-16 版 权：Copyright 2010 中国气象信息中心 */
package jms;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;

/**
 * @ClassName JmsReceiver.java
 * @version 1.0
 * @Description
 * @author tsw
 * @date 2012-1-16 下午3:23:23
 * @Copyright 中国气象信息中心
 */

public class JmsReceiver {

    public static void main (String[] args) throws IOException {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("classpath:app*.xml");
        JmsTemplate jmsTemplate = (JmsTemplate) ctx.getBean("jmsTemplate");
        while (true) {
            Map<String, Object> mm = (Map<String, Object>) jmsTemplate.receiveAndConvert();
            System.out.println("收到消息：" + new Date((Long) mm.get("count")));
        }
    }
}
