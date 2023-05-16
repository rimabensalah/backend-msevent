package com.service.event.api;

import com.service.event.domain.Message;
import com.service.event.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/sse-server")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true")
public class SSEController {
    @Autowired
    private MessageService sseService;

    /**
     * Register user
     *
     * @return success message
     */
    @PostMapping("/user")
    public String addUser(@RequestParam("name") String name) {
        sseService.addUser(name);
        return "User " + name + " added !";
    }

    /**
     * Send message to user
     *
     * @param message body with from user, to user and message text
     * @return success message
     */
    @PostMapping("/user/message")
    public String sendUserMessage(@RequestBody Message message) {
        sseService.sendUserMessage(message);
        return "Message sent from " + message.getFrom() + " to " + message.getTo();
    }

    /**
     * List all users who have registered
     *
     * @param name of user sending request, used to filter out user from list
     * @return success message
     */
    @GetMapping("/users")
    public Flux<ServerSentEvent<List<String>>> streamUsers(@RequestParam("name") String name) {
        return sseService.getUsers(name);
    }

    /**
     * Last message sent by users
     *
     * @param name of user sending request, used to filter out user from list
     * @return last message sent by users
     */
    @GetMapping("/user/messages")
    public Flux<ServerSentEvent<Message>> streamLastMessage(@RequestParam("name") String name) {
        return sseService.getLastUserMessage(name);
    }

    /**
     * List all messages sent by users
     *
     * @param name of user sending request, used to filter out user from list
     * @return messages sent by users
     */
    @GetMapping("/user/messages/all")
    public Flux<ServerSentEvent<List<Message>>> streamMessages(@RequestParam("name") String name) {
        return sseService.getAllUserMessages(name);
    }
}
