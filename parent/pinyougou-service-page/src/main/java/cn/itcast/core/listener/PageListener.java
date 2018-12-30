package cn.itcast.core.listener;

import cn.itcast.core.service.StaticPageService;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
@Service
public class PageListener implements MessageListener {
    @Autowired
    private StaticPageService staticPageService;
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage atm = (ActiveMQTextMessage) message;
        try {
            String id = atm.getText();
            System.out.println("生成静态化页面ID"+id);
            //3:静态化处理(生成静态的页面)
            staticPageService.index(Long.parseLong(id));
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
