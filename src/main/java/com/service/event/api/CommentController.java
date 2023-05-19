package com.service.event.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.event.domain.*;
import com.service.event.payload.response.CommentAddedEvent;
import com.service.event.repository.CommentRepository;
import com.service.event.repository.EventRepository;
import com.service.event.repository.FileRepository;
import com.service.event.repository.UserRepository;
import com.service.event.service.EventService;
import com.service.event.service.NotificationStorgeService;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@CrossOrigin("*")
@RestController
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    EventService eventService;
    @Autowired
    public EventRepository eventRepo;
    @Autowired
    public CommentRepository commentRepo;
    @Autowired
    public FileRepository fileRepo;
    @Autowired
    public UserRepository userRepo;
    @Autowired
    public NotificationStorgeService notifService;
    private SimpMessagingTemplate simpMessagingTemplate;
    private CommentRepository commentRepository;

    @Autowired
    public CommentController(SimpMessagingTemplate simpMessagingTemplate, CommentRepository commentRepository) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.commentRepository = commentRepository;
    }



    @PostMapping("/post/{eventID}")
    public ResponseEntity<?> addComment(
            @PathVariable(value = "eventID") Long eventID,
            //@RequestBody Comment commentRequest,
            @RequestParam("comment") String commentReq,
            @RequestParam(value = "commentimage", required = false) MultipartFile image
    ) throws IOException {
        try {
            Comment commentRequest = new ObjectMapper().readValue(commentReq, Comment.class);
            Evenement eventData = eventRepo.findById(eventID).get();
            List<Comment> c = new ArrayList<Comment>();

            if (image == null) {
                commentRequest.setCreatedDate2(LocalDateTime.now());
                commentRequest.setCan_like(true);
                commentRequest.setCan_remove(true);
                commentRequest.setUser_like(false);
                commentRepo.save(commentRequest);
                if (eventData.getComments() == null) {
                    c.add(commentRequest);
                    eventData.setComments(c);
                } else {
                    eventData.getComments().add(commentRequest);
                }
            } else {
                commentRequest.setCreatedDate2(LocalDateTime.now());
                commentRequest.setCan_like(true);
                commentRequest.setCan_remove(true);
                commentRequest.setUser_like(false);
                commentRepo.save(commentRequest);
                if (eventData.getComments() == null) {
                    c.add(commentRequest);
                    eventData.setComments(c);
                } else {
                    eventData.getComments().add(commentRequest);
                }
                String title = image.getOriginalFilename();
                EventFile photo = new EventFile(title);
                photo.setFile(new Binary(BsonBinarySubType.BINARY, image.getBytes()));
                photo = fileRepo.insert(photo);
                commentRequest.setEventfile(photo);

                commentRepo.save(commentRequest);
                //eventData.getComments().add(commentRequest);

            }
            notifService.createNotificationStorage(Notification.builder()
                    .delivered(false)
                    .content("new comment from " + commentRequest.getFromUser().getUsername())
                    .notificationType(NotificationType.comment)
                    .userFrom(commentRequest.getFromUser())
                    .userTo(eventData.getUser())
                    .createdAt(new Date()).build());

            Comment savedComment = commentRepository.save(commentRequest);
            simpMessagingTemplate.convertAndSendToUser(commentRequest.getFromUser().toString(), "/comments/" + eventID, savedComment);
            return ResponseEntity.ok(eventRepo.save(eventData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
