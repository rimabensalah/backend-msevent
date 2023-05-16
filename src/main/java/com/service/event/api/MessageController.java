package com.service.event.api;

import com.service.event.domain.Message;
import com.service.event.payload.response.MessageReponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class MessageController {
    @MessageMapping("/message")
    @SendTo("/receive/message")
    public MessageReponse getMessage(final Message message) throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("recived message");
        return new MessageReponse(HtmlUtils.htmlEscape(message.getText()));
    }
}
