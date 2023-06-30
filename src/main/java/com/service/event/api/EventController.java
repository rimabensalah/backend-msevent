package com.service.event.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Content;
import com.service.event.domain.*;
import com.service.event.payload.response.MessageReponse;
import com.service.event.repository.*;
import com.service.event.service.*;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/event")
public class EventController {
    @Autowired
    EventService eventService;
    @Autowired
    public EventRepository eventRepo;
    @Autowired
    public CommentRepository commentRepo;
    @Autowired
    public TagRepository tagRepo;
    @Autowired
    public FileRepository fileRepo;
    @Autowired
    public UserRepository userRepo;
    @Autowired
    public NotificationStorgeService notifService;
    @Autowired
    public SendinblueTransactionalEmailsApi sendinblue;
    @Autowired
    ServerWebSocketHandler  serverWebSocketHandler;
    @Autowired
    MailService mailService;
    @Autowired
    private SendinblueTransactionalEmailsApi  mailService2;
    /* @GetMapping("/stream")
     public Flux<ServerSentEvent<List<Evenement>>> streamPosts() {
         return postService.streamPosts();
     }*/
    @PostMapping("/addEvent")
    public ResponseEntity<?> addEvent(
            @RequestBody Evenement eventRequest) {

        return new ResponseEntity<>(eventService.saveEvent(eventRequest), HttpStatus.OK);
    }

    //add event with image
    @PostMapping("/addeventWithfile")
    public ResponseEntity<?> addeventwithfile(
            @RequestParam("event") String eventRequest,
            @RequestParam("user") String userRequest,
            @RequestParam("image") MultipartFile image
    ) throws IOException {
        try {
            Evenement evenement = new ObjectMapper().readValue(eventRequest, Evenement.class);
            Utilisateur user = new ObjectMapper().readValue(userRequest, Utilisateur.class);

            String title = evenement.getTitle();
            EventFile photo = new EventFile(title);
            photo.setFile(new Binary(BsonBinarySubType.BINARY, image.getBytes()));
            photo = fileRepo.insert(photo);
            evenement.setEventfile(photo);
            userRepo.save(user);
            evenement.setUser(user);


            return new ResponseEntity<>(eventService.saveEvent(evenement), HttpStatus.OK);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }

    //add event + tag
    @PostMapping("/addevent")
    public ResponseEntity<?> addEventTag(
            //@RequestBody Evenement eventRequest,
            @RequestParam("event") String eventRequest,

            @RequestParam("tagids") String tagIds) {
        try {
            Evenement evenement = new ObjectMapper().readValue(eventRequest, Evenement.class);
            List<String> strArray = Arrays.asList(tagIds);
            //new ArrayList() {tagIds};

            Set<Tag> tagsToAdd = strArray.stream()
                    .map(tagId -> tagRepo.findById((tagId)))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());

            evenement.setTags(tagsToAdd);
            Evenement eventUpdated = eventRepo.save(evenement);
            return new ResponseEntity<>(strArray, HttpStatus.OK);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    //add tag to event
    @PostMapping("/event/{id}/tags")
    public ResponseEntity<Evenement> addTagToEvent(@PathVariable Long id,
                                                   @RequestBody List<String> tagIds) {
        Optional<Evenement> optionalEvent = eventRepo.findById(id);
        if (!(optionalEvent.isPresent())) {
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
        Evenement evenToUpdate = optionalEvent.get();
        Set<Tag> tagsToAdd = tagIds.stream()
                .map(tagId -> tagRepo.findById((tagId)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        evenToUpdate.setTags(tagsToAdd);
        Evenement eventUpdated = eventRepo.save(evenToUpdate);
        return new ResponseEntity<>(eventUpdated, HttpStatus.OK);


    }


    //http://localhost:8087/api/event/getallevent
    @GetMapping("/getallevent")
    public ResponseEntity<List<Evenement>> getAllEvent(@RequestParam(required = false) String name) {
        try {
            List<Evenement> evenements = new ArrayList<Evenement>();
            if (name == null) {
                eventRepo.findAll().forEach(evenements::add);
            }


            if (evenements.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(evenements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //get event by id
    @GetMapping("/getevent/{id}")
    public ResponseEntity<?> geteventtById(@PathVariable("id") Long id) {
        Optional<Evenement> eventData = eventRepo.findById(id);
        if (eventData.isPresent())
            return new ResponseEntity<>(eventData, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //update event
    @PutMapping("/updateevent/{id}")
    public ResponseEntity<?> updateevent(@PathVariable("id") long id,
                                         // @PathVariable("idimg")  String idimg,
                                         @RequestParam("event") String eventRequest,

                                         @RequestParam(value = "image", required = false) MultipartFile image
    )
            throws IOException {
        try {
            Optional<Evenement> eventData = eventRepo.findEvenementById(id);
            //Optional<EventFile> imageData= fileRepo.findById(idimg);
            String fileID = eventData.get().getEventfile().getId();
            Optional<EventFile> imageData = fileRepo.findById(fileID);
            Evenement evenement = new ObjectMapper().readValue(eventRequest, Evenement.class);

            if (image == null) {
                if (eventData.isPresent()) {
                    Evenement updateEvent = eventData.get();
                    updateEvent.setTitle(evenement.getTitle());
                    updateEvent.setContent(evenement.getContent());
                    updateEvent.setUpdatedDate(LocalDateTime.now());
                    updateEvent.setStatus(EventStatus.Updated);


                    //updateEvent.setEventfile(imageData);
                    eventRepo.save(updateEvent);


                }
            } else {
                if (eventData.isPresent()) {
                    Evenement updateEvent = eventData.get();
                    updateEvent.setTitle(evenement.getTitle());
                    updateEvent.setContent(evenement.getContent());
                    updateEvent.setUpdatedDate(LocalDateTime.now());
                    updateEvent.setStatus(EventStatus.Updated);

                    if (imageData.isPresent()) {
                        EventFile updatedFile = imageData.get();
                        String title = evenement.getTitle();
                        updatedFile.setFilename(title);
                        updatedFile.setFile(new Binary(BsonBinarySubType.BINARY, image.getBytes()));
                        //EventFile photo = new EventFile(title);
                        // photo.setFile(new Binary(BsonBinarySubType.BINARY, image.getBytes()));
                        fileRepo.save(updatedFile);
                        updateEvent.setEventfile(updatedFile);
                        eventRepo.save(updateEvent);
                    }


                }

            }

            return new ResponseEntity<>(eventRepo.findEvenementById(id), HttpStatus.OK);


        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    //remove event
    @DeleteMapping("/deleteevent/{eventID}")
    public ResponseEntity<?> deleteUser(@PathVariable(value = "eventID") Long eventID) {
        Evenement event = eventRepo.findById(eventID)
                .orElseThrow(() -> new RuntimeException("Not found Event with id = " + eventID));

        Optional<Evenement> eventData = eventRepo.findEvenementById(eventID);
        if (eventData.isPresent()) {
            /*event.setStatus(EventStatus.Deleted);
            eventRepo.save(event);*/
            eventRepo.deleteById(eventID);
            MessageReponse message = new MessageReponse("event deleted ! ");
            return new ResponseEntity<>(message, HttpStatus.OK);

        } else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);


    }

    //get event by user
    @GetMapping("/geteventbyuser/{id}")
    public ResponseEntity<List<Evenement>> getEventByUser(
            @PathVariable(value = "id") Long id,
            @RequestParam(required = false) String name) {
        try {
            List<Evenement> evenements = new ArrayList<Evenement>();
            if (name == null) {
                // eventRepo.findAll().forEach(evenements::add);
                eventRepo.findEvenementByUserId(id).forEach(evenements::add);

            }


            if (evenements.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(evenements, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/comment/{eventID}")
    public ResponseEntity<?> addComment(
            @PathVariable(value = "eventID") Long eventID,
            //@RequestBody Comment commentRequest,
            @RequestParam("comment") String commentReq,
            @RequestParam(value = "commentimage", required = false) MultipartFile image,
            @RequestParam(value = "notifsettings",required = false) boolean notifsettings
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
            }
            else {
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
              /*notifService.sendNotification("ryma.bensalah@esprit.tn",
                      "New Like on Post",
                      "Your post has been liked by someone.");*/
          /*final String url = "http://localhost:3000/singleevent/${eventID}"
            sendinblue.sendMail("new comment is added", eventData.getUser().getUsername(), "ryma.bensalah@esprit.tn",
                    "Visit this url : "+ url);*/
           /* String toEmail = "ryma.bensalah@esprit.tn";
            sendinblue.sendCommentNotification(toEmail,"Visit this url : "+ url);*/
            final String url = "http://localhost:3000/singleevent/"+eventID;
            String content = "new comment is added ," +
                    " to more details Please click on the following link : "+ url;

            if(notifsettings){
               // mailService.sendTextEmail("new comment is added",content);
                mailService2.sendMail("new comment is added",
                        "ryma","rymabnslh@gmail.com",content);
           }

            serverWebSocketHandler.notifyCommentAdded(eventData,commentRepo.save(commentRequest));
            //serverWebSocketHandler.notifyCommentAdded2(eventData,commentRepo.save(commentRequest),eventData.getUser().getUsername());
            return ResponseEntity.ok(eventRepo.save(eventData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping  ("/bookmarked/{eventID}/{userID}")
   public ResponseEntity<?> addBookmarked(
           @PathVariable(value = "eventID") Long eventID,
           @PathVariable(value = "userID") Long userID
   ){
        return ResponseEntity.ok(eventService.addBookmarked(eventID,userID));
   }
    //getcomment by id
    @GetMapping("/commentByid/{id}")
    public ResponseEntity<?> getCommentById(@PathVariable("id") String id) {
        Optional<Comment> commentData = commentRepo.findById(id);
        if (commentData.isPresent())
            return new ResponseEntity<>(commentData, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //get all Comment
    @GetMapping("/comment")
    public ResponseEntity<?> getComment() {
        List<Comment> commentData = commentRepo.findAll();
        if (commentData.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        else
            return new ResponseEntity<>(commentData, HttpStatus.OK);

    }

    //get comments by event id
    @GetMapping("/commentbyeventid/{id}")
    public ResponseEntity<List<Comment>> getCommentsByEvent(@PathVariable("id") Long id) {
        return ResponseEntity.ok(eventService.getCommentsByEventId(id));
    }
    //update comment content
    @PutMapping("/editcomment/{id}")
    public ResponseEntity<?> editcomment(@PathVariable("id") String id,
                                         @RequestParam("comment") String commentRequest,
                                         @RequestParam(value = "commentimage", required = false) MultipartFile image
    ) throws IOException {
        try {
            Comment comment = new ObjectMapper().readValue(commentRequest, Comment.class);
            Optional<Comment> commentData = commentRepo.findById(id);
            if (image == null) {
                if (commentData.isPresent()) {
                    Comment updateComment = commentData.get();

                    updateComment.setOldContent(updateComment.getContent());
                    updateComment.setContent(comment.getContent());
                    updateComment.setUpdatedDate(LocalDateTime.now());
                    //updateEvent.setEventfile(imageData);
                    commentRepo.save(updateComment);
                }

            } else {
                if (commentData.isPresent()) {
                    Comment updateComment = commentData.get();

                    updateComment.setOldContent(updateComment.getContent());
                    updateComment.setContent(comment.getContent());
                    updateComment.setUpdatedDate(LocalDateTime.now());
                    //updateEvent.setEventfile(imageData);
                    String title = updateComment.getContent();
                    EventFile photo = new EventFile(title);
                    photo.setFile(new Binary(BsonBinarySubType.BINARY, image.getBytes()));
                    photo = fileRepo.insert(photo);
                    updateComment.setEventfile(photo);

                    commentRepo.save(updateComment);
                }
            }
            return new ResponseEntity<>(commentRepo.findById(id), HttpStatus.OK);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    //delete comment
    @DeleteMapping("/deletecomment/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable("id") String id) {
        Optional<Comment> commentData = commentRepo.findById(id);
        //String Message="";
        if (commentData.isPresent()) {
            commentRepo.deleteById(id);
            MessageReponse message = new MessageReponse("comment deleted ! ");
            return new ResponseEntity<>(message, HttpStatus.OK);
        } else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //a comment replayed to another comment
    @PostMapping("/event/{idevent}/comment/{idcomment}")
    public ResponseEntity<?> addReplyComment(
            @PathVariable(value = "idevent") Long idevent,
            @PathVariable(value = "idcomment") String idcomment,
            @RequestBody Comment commentRequest
    ) {
        Evenement eventData = eventRepo.findById(idevent).get();

        Optional<Comment> commentData = commentRepo.findById(idcomment);
        List<Comment> c = new ArrayList<Comment>();


        if (commentData.isPresent()) {
            Comment updateComment = commentData.get();
            //commentRequest.setId(new ObjectId().toString());
            commentRequest.setCreatedDate2(LocalDateTime.now());
            commentRequest.setCan_like(true);
            commentRequest.setCan_remove(true);
            commentRequest.setUser_like(false);
            commentRepo.save(commentRequest);

            if (updateComment.getComments() == null) {
           /* c.add(commentRequest);
            commentData.setComments(c);
            eventData.getComments().add(commentData);*/
                c.add(commentRequest);
                updateComment.setComments(c);
                commentRepo.save(updateComment);


            } else {
                updateComment.getComments().add(commentRequest);
                //eventData.getComments().add(updateComment);
                commentRepo.save(updateComment);
            }

        }


        //push notif
        notifService.createNotificationStorage(Notification.builder()
                .delivered(false)
                .content("new reply from " + commentData.get().getFromUser().getUsername())
                .notificationType(NotificationType.reply)
                .userFrom(commentData.get().getFromUser())
                .userTo(eventData.getUser())
                .createdAt(new Date()).build());

        return ResponseEntity.ok(commentRepo.findById(idcomment).get());
    }

    @PostMapping("/{eventid}/comment/{commentId}/like")
    public ResponseEntity<Comment> addLike(
            @PathVariable("eventid") Long eventid,
            @PathVariable("commentId") String commentId,
            @RequestParam("user") String userRequest
    ) throws IOException {

        Utilisateur user = new ObjectMapper().readValue(userRequest, Utilisateur.class);
        return ResponseEntity.ok(eventService.addLike(eventid, commentId, user));


    }
    //add vote
    @PostMapping("/comment/{commentid}/vote")
    public ResponseEntity<Comment> addVote(
            @PathVariable("commentid") String commentid,
            @RequestParam("user") String userRequest
    ) throws IOException {
        Utilisateur user = new ObjectMapper().readValue(userRequest, Utilisateur.class);
        return ResponseEntity.ok(eventService.addVote(commentid, user));
    }

    //remove vote
    @PostMapping("/comment/{commentid}/removevote")
    public ResponseEntity<Comment> removeVote(
            @PathVariable("commentid") String commentid,
            @RequestParam("user") String userRequest
    ) throws IOException {
        Utilisateur user = new ObjectMapper().readValue(userRequest, Utilisateur.class);
        return ResponseEntity.ok(eventService.removeVote(commentid, user));
    }

    //find by title
    @GetMapping("/events")
    public ResponseEntity<Map<String, Object>> getAllEventPage(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        try {
            List<Evenement> events = new ArrayList<Evenement>();
            Pageable paging = PageRequest.of(page, size);

            Page<Evenement> pageTuts;
            if (title == null)
                pageTuts = eventRepo.findAll(paging);
            else
                pageTuts = eventRepo.findByTitleContainingIgnoreCase(title, paging);

            events = pageTuts.getContent();

            Map<String, Object> response = new HashMap<>();
            response.put("events", events);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalItems", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //get event by username
    @GetMapping("/events/username")
    public ResponseEntity<?> getAllEventUser(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String title,
            @RequestParam(required = false, value = "tag") List<String> tagNameList,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        try {


            List<Evenement> events = new ArrayList<Evenement>();
            Set<Tag> tags = new HashSet<>();
            Pageable paging = PageRequest.of(page, size);

            Page<Evenement> pageTuts;
            if (username == null && title == null && tagNameList == null) {

                pageTuts = eventRepo.findAll(paging);
            } else {
                if (username != null && title == null && tagNameList == null) {

                    Optional<Utilisateur> searchUser = userRepo.findByUsername(username);
                    Long userid = searchUser.get().getId();
                    pageTuts = eventRepo.findEvenementByUserId(userid, paging);
                } else {
                    if (title != null && username == null && tagNameList == null) {
                        pageTuts = eventRepo.findByTitleContainingIgnoreCase(title, paging);
                    } else {
                        tagNameList.forEach(tagName -> {
                            if (tagRepo.findByTagName(tagName).isPresent()) {
                                tags.add(tagRepo.findByTagName(tagName).get());
                            }
                        });
                        // List<Evenement> events = eventRepo.findAllByTagsIn(tags);
                        pageTuts = eventRepo.findAllByTagsIn(tags, paging);
                    }


                }
            }


            events = pageTuts.getContent();

            Map<String, Object> response = new HashMap<>();
            response.put("events", events);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalItems", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //find related  event
    @GetMapping("/relatedevent")
    public ResponseEntity<?> getAllEventUser(
            @RequestParam(value = "tag") List<String> tagNameList) {
        Set<Tag> tags = new HashSet<>();
        tagNameList.forEach(tagName -> {
            if (tagRepo.findByTagName(tagName).isPresent()) {
                tags.add(tagRepo.findByTagName(tagName).get());
            }
        });
        return new ResponseEntity<>(eventRepo.findFirst4ByTagsIn(tags), HttpStatus.OK);
    }

    @GetMapping("/tag-search")
    public ResponseEntity<?> searchByTags(
            @RequestParam(required = false, value = "tag") List<String> tagNameList,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        List<Evenement> events = new ArrayList<Evenement>();
        Set<Tag> tags = new HashSet<>();
        Pageable paging = PageRequest.of(page, size);

        Page<Evenement> pageTuts;
        if (tagNameList == null) {

            pageTuts = eventRepo.findAll(paging);
        } else {
            tagNameList.forEach(tagName -> {
                if (tagRepo.findByTagName(tagName).isPresent()) {
                    tags.add(tagRepo.findByTagName(tagName).get());
                }
            });
            // List<Evenement> events = eventRepo.findAllByTagsIn(tags);
            pageTuts = eventRepo.findAllByTagsIn(tags, paging);
        }


        events = pageTuts.getContent();

        Map<String, Object> response = new HashMap<>();
        response.put("events", events);
        response.put("currentPage", pageTuts.getNumber());
        response.put("totalItems", pageTuts.getTotalElements());
        response.put("totalPages", pageTuts.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
       /* Set<Tag> tags = new HashSet<>();

        tagNameList.forEach(tagName -> {
            if (tagRepo.findByTagName(tagName).isPresent()) {
                tags.add(tagRepo.findByTagName(tagName).get());
            }
        });

        List<Evenement> events = eventRepo.findAllByTagsIn(tags);
        return ResponseEntity.ok(events);*/
    }

    //find by title
    @GetMapping("/eventsUser/{id}")
    public ResponseEntity<Map<String, Object>> getAllEventUserPage(
            @PathVariable(value = "id") Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false, value = "tag") List<String> tagNameList,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        try {
            List<Evenement> events = new ArrayList<Evenement>();
            Pageable paging = PageRequest.of(page, size);
            Set<Tag> tags = new HashSet<>();

            Page<Evenement> pageTuts;
            if (title == null && tagNameList == null)
                pageTuts = eventRepo.findEvenementByUserId(id, paging);
            else {
                if (title != null && tagNameList == null) {
                    pageTuts = eventRepo.findByTitleContainingIgnoreCaseAndUserId(title, id, paging);
                } else {
                    tagNameList.forEach(tagName -> {
                        if (tagRepo.findByTagName(tagName).isPresent()) {
                            tags.add(tagRepo.findByTagName(tagName).get());
                        }
                    });
                    // List<Evenement> events = eventRepo.findAllByTagsIn(tags);
                    pageTuts = eventRepo.findAllByTagsInAndUserId(tags, id, paging);
                }
            }


            events = pageTuts.getContent();

            Map<String, Object> response = new HashMap<>();
            response.put("events", events);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalItems", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //validate the best comment
    @PatchMapping("/validate/{id}")
    public ResponseEntity validatecomment(@PathVariable String id) {
        return ResponseEntity.ok(eventService.validateComment(id));
    }

    @PutMapping("validate/{idevent}/{idcomment}")
    public ResponseEntity<?> validateeventt(@PathVariable Long idevent, @PathVariable String idcomment) {

        try {
            Evenement eventData = eventRepo.findEvenementById(idevent).get();
            Comment commentData = commentRepo.findById(idcomment).get();
            if (eventData.getIsValidate() == null) {
                return ResponseEntity.ok(eventService.validate(idevent, idcomment));
            } else {
                if ((eventData.getIsValidate())) {
                    return ResponseEntity.badRequest().body(new MessageReponse("Error: comment is already validated!"));

                } else {
                    return ResponseEntity.ok(eventService.validate(idevent, idcomment));
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @PutMapping("invalid/{idevent}/{idcomment}")
    public ResponseEntity invalideventt(@PathVariable Long idevent, @PathVariable String idcomment) {
        return ResponseEntity.ok(eventService.invalidComment(idevent, idcomment));
    }

    @GetMapping("/answeredevent")
    public ResponseEntity answredevent(
            @RequestParam(required = false) Boolean isValidate,
            @RequestParam(required = false) Boolean isSorted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        try {
            List<Evenement> events = new ArrayList<Evenement>();
            Pageable paging = PageRequest.of(page, size);
            Page<Evenement> pageTuts;
            if (isValidate == null)
                if (isSorted == null) {
                    pageTuts = eventRepo.findAll(paging);
                } else {
                    if (isSorted == true) {
                        pageTuts = eventRepo.findAllByOrderByCreatedDateDesc(true, paging);
                    } else {
                        pageTuts = eventRepo.findAll(paging);
                    }
                }

            else {
                if (isValidate == true) {
                    pageTuts = eventRepo.findByIsValidate(true, paging);
                } else {
                    pageTuts = eventRepo.findByIsValidate(false, paging);
                }

            }

            events = pageTuts.getContent();
            Map<String, Object> response = new HashMap<>();
            response.put("events", events);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalItems", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    @GetMapping("/adminallevent")
    public ResponseEntity adminallevent(
            @RequestParam(required = false) Boolean isValidate,
            @RequestParam(required = false) String title,
            //@RequestParam(required = false) Boolean isSorted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        try {
            List<Evenement> events = new ArrayList<Evenement>();
            Pageable paging = PageRequest.of(page, size);
            Page<Evenement> pageTuts;
            if (isValidate == null){
                if(title==null){
                    pageTuts = eventRepo.findAll(paging);
                }else{
                    pageTuts=eventRepo.findAllByTitleContainingIgnoreCase(title,paging);
                }
            }
            else {
                if (isValidate == true) {
                    if(title==null){ pageTuts = eventRepo.findByIsValidate(true, paging);}
                    else{ pageTuts = eventRepo.findByTitleContainingIgnoreCaseAndIsValidate(title,true, paging);}

                } else {
                    if(title==null){ pageTuts = eventRepo.findByIsValidate(false, paging);}
                    else{ pageTuts = eventRepo.findByTitleContainingIgnoreCaseAndIsValidate(title,false, paging);}
                }

            }

            events = pageTuts.getContent();
            Map<String, Object> response = new HashMap<>();
            response.put("events", events);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalItems", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    @GetMapping("/stream")
    public Flux<ServerSentEvent<List<Evenement>>> streamPosts() {
        return eventService.streamPosts();
    }

    @GetMapping(value = "/objects/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Evenement>> streamObjectById(@PathVariable("id") Long id) {
        Optional<Evenement> objectOptional = eventRepo.findById(id);
        if (objectOptional.isPresent()) {
            return Flux.interval(Duration.ofSeconds(1))
                    .map(i -> ServerSentEvent.builder(objectOptional.get()).build());
        } else {
            return Flux.empty();
        }
    }
    //total event par user
    @GetMapping("/counttotalevent/{id}")
    public ResponseEntity<?> countTotalEvent( @PathVariable("id") Long id){
        return new ResponseEntity<>(eventService.totalEventBuUser(id), HttpStatus.OK);

    }
    //validate event by user
    @GetMapping("/validatedevent/{userid}")
    public ResponseEntity<?> validatedEvent(@PathVariable("userid") Long userid){
        return  new ResponseEntity<>(eventService.validatedEventByUser(userid),HttpStatus.OK);
    }
    //total comment par user
    @GetMapping("/counttotalcomment/{userid}")
    public ResponseEntity<?> countTotalComment(@PathVariable("userid") Long userid){
        return new ResponseEntity<>(eventService.totalCommentByUser(userid) , HttpStatus.OK);
    }
    @GetMapping("/getrecentevent")
    public ResponseEntity<?> getrecentevent (){
        return new ResponseEntity<>(eventService.getRecentEvent() , HttpStatus.OK);
    }
   @GetMapping("/eventUserAttribute/{id}")
    public ResponseEntity<?> eventUserattribute (@PathVariable("id") Long id)
    {
        return new ResponseEntity<>(eventService.eventUserAttribute(id) , HttpStatus.OK);
    }

    @GetMapping("/getcommentbyeventid/{id}")
    public ResponseEntity<?> commentsbyeventid(@PathVariable("id") Long id){
        return new ResponseEntity<>(eventService.findAnswersByPostId(id),HttpStatus.OK);
    }
    @GetMapping("/findPostsByContributor/{id}")
    public ResponseEntity<?> geteventByContributor (
            @PathVariable("id") Long id,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize
    ){
        List<Evenement> posts = eventService.findPostsByContributor(id, pageNumber, pageSize);
        if (posts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(posts);
        //return new ResponseEntity<>(eventService.findPostsByContributor(id),HttpStatus.OK);
    }
    //findPopularEvent
    @GetMapping("/findpopularevent")
    public ResponseEntity<?> findPopularEvent(){
        return new ResponseEntity<>(eventService.findPopularEvent(),HttpStatus.OK);
    }
    @GetMapping("/counteventThisMonth/{userid}")
    public ResponseEntity<?> countPostsThisMonthByContributor(@PathVariable("userid")Long userid){
        return new ResponseEntity<>(eventService.countPostsThisMonthByContributor(userid),HttpStatus.OK);
    }
    @GetMapping("/countcommentThisMonth/{userid}")
    public ResponseEntity<?> countCommentsThisMonthByuser(@PathVariable("userid") Long userid){
        return new ResponseEntity<>(eventService.getNumberOfCommentsAddedThisMonth(userid),HttpStatus.OK);
    }
    @GetMapping("/admin/posts/recent")
    public ResponseEntity<List<Evenement>> getRecentPosts(@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        List<Evenement> posts = eventRepo.findByCreatedDateGreaterThanEqualOrderByCreatedDateDesc(date);
        return ResponseEntity.ok(posts);
    }
    @GetMapping("/posts/topusers")
    public ResponseEntity<?> countpostsbyuser(){

        return new ResponseEntity<>(eventRepo.countEventsByUser(),HttpStatus.OK);
    }
    @GetMapping("/topusers")
    public List<UserPostCount> getTopUsersByPostCount() {
        return eventService.findTop3UsersByPostCount();
    }
    @GetMapping("/{eventid}/comments")
    public List<Comment> getPostComments(@PathVariable("eventid") Long eventid) {
        return eventService.findAnswersByPostId(eventid);
    }
    /*@GetMapping("/eventStats")
    public EventStats getEventStats(@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date start,
                                    @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date end) {
        return eventService.getEventStats(start, end);
    }*/
    @GetMapping("/eventStats")
    public EventStats getEventStats() {
        return eventService.getEventStats();
    }
   /* @GetMapping("/stats")
    public List<EventStats> getEventStatsByMonth(@RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                                                 @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        List<Evenement> events = eventRepo.findByCreatedDateBetweenAndIsValidate(start, end);

        Map<YearMonth, Integer> totalEventCountMap = events.stream()
                .collect(Collectors.groupingBy(event -> YearMonth.from(event.getCreatedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()), Collectors.summingInt(e -> 1)));

        Map<YearMonth, Integer> validEventCountMap = events.stream()
                .filter(Evenement::isValidate)
                .collect(Collectors.groupingBy(event -> YearMonth.from(event.getCreatedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()), Collectors.summingInt(e -> 1)));

        List<EventStats> eventStatsList = new ArrayList<>();

        for (YearMonth yearMonth : totalEventCountMap.keySet()) {
            int totalEventCount = totalEventCountMap.getOrDefault(yearMonth, 0);
            int validEventCount = validEventCountMap.getOrDefault(yearMonth, 0);

            EventStats eventStats = new EventStats();
            eventStats.setYear(yearMonth.getYear());
            eventStats.setMonth(yearMonth.getMonthValue());
            eventStats.setTotalEventCount(totalEventCount);
            eventStats.setValidEventCount(validEventCount);

            eventStatsList.add(eventStats);
        }

        return eventStatsList;
    }*/

}
