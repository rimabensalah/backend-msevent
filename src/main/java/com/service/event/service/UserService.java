package com.service.event.service;


import com.mongodb.BasicDBObject;
import com.service.event.domain.Comment;
import com.service.event.domain.Evenement;
import com.service.event.domain.Tag;
import com.service.event.domain.Utilisateur;
import com.service.event.repository.EventRepository;
import com.service.event.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAccumulator;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private EventRepository eventRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    public Utilisateur getUserById(Long id ) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("user not found!"));
    }
    public Utilisateur getUserWithBookmarkedPosts(Long userId) {
        Utilisateur user = userRepo.findById(userId).orElse(null);
        if (user != null) {
            List<Evenement> bookmarkedPosts = new ArrayList<>();
            for (Evenement event : user.getBookmarked()) {
                event.setUser(null);
                for (Tag tag : event.getTags()) {
                    tag.setEvents_ids(null);
                }
                bookmarkedPosts.add(event);
                event.setComments(null);
            }
            user.setBookmarked(bookmarkedPosts);
        }
        return user;
    }
}
