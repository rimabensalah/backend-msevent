package com.service.event.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.service.event.domain.BookmarkedEvent;
import com.service.event.domain.Evenement;
import com.service.event.domain.Utilisateur;
import com.service.event.repository.UserRepository;
import com.service.event.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserService userService ;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private SendinblueTransactionalEmailsApi  mailService;
    @Autowired
    BookmarkedEventService bookmarkedEventService;
    @PostMapping("/addBookmarked")
    public ResponseEntity<?> addBookmarked(
           // @RequestParam("event") String eventRequest,
           // @RequestParam("user") String userRequest
            @RequestBody BookmarkedEvent bookmarkedRequest
    ) throws IOException {
        /*ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Evenement evenement = mapper.readValue(eventRequest, Evenement.class);
        evenement.setEventfile(null);
        Utilisateur user = mapper.readValue(userRequest, Utilisateur.class);*/



        return new ResponseEntity<>(bookmarkedEventService.addBookmarked(bookmarkedRequest), HttpStatus.OK);
    }
    @GetMapping("/retreivebookmarked/{userid}")
    public ResponseEntity<?> getBookmarkedEvent(@PathVariable("userid") long userid ){
        return  new ResponseEntity<>(bookmarkedEventService.findBookmarkedByuser(userid), HttpStatus.OK);
    }

    @PostMapping("/send-text")
    public void send() throws IOException {
         mailService.sendMail("test","ryma","rymabnslh@gmail.com","test send email");

    }



}
