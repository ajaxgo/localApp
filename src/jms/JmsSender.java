package jms;

import java.util.Date;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * @ClassName JmsSender.java
 * @version 1.0
 * @Description
 * @author tsw
 * @date 2012-1-16 下午3:23:09
 * @Copyright
 */

public class JmsSender {

    public static void main (String[] args) {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("classpath:app*.xml");

        JmsTemplate jmsTemplate = (JmsTemplate) ctx.getBean("jmsTemplate");

        jmsTemplate.send(new MessageCreator() {

            public Message createMessage (Session session) throws JMSException {
                MapMessage mm = session.createMapMessage();
                // try {
                // Context ctx = new InitialContext();
                // Context envCtx = (Context) ctx.lookup("java:comp/env");
                // NamingEnumeration list = envCtx.list("ou=People");
                // while (list.hasMore()) {
                // NameClassPair nc = (NameClassPair) list.next();
                // mm.setString(nc.getName(), nc.getNameInNamespace());
                // }
                // } catch (NamingException e) {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }
                mm.setLong("count", new Date().getTime());
                return mm;
            }

        });
    }

}
