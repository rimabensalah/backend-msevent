package com.service.event.api;

import com.service.event.domain.Utilisateur;
import com.service.event.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin("*")
@Controller
public class NotificationController {
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    public NotificationController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/notifications")
    public void sendNotification(String message) {
        this.messagingTemplate.convertAndSend("/topic/notifications", message);
    }
    /*@MessageMapping("/send/{sessionId}")
    public void sendMessageToUser(@DestinationVariable String sessionId, String message) {
        this.messagingTemplate.convertAndSendToUser(sessionId, "/queue/messages", message);
    }*/
    public void sendMessageToUser(Long eventID, String message) {
        Utilisateur user= eventRepository.findById(eventID).get().getUser();
        messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/messages", message);
    }
}
