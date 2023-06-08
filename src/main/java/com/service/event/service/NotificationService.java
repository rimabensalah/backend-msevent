package com.service.event.service;

import com.service.event.domain.Notification;
import com.service.event.domain.NotificationEvent;
import com.service.event.repository.NotificationRepository;
import com.service.event.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class NotificationService {


    @Autowired
    private NotificationRepository notifRepo;
    public Notification  getNotificationsForUser(String userId) throws ChangeSetPersister.NotFoundException {
      /*  return mongoTemplate.find(
                Query.query(Criteria.where("userId").is(userId)),
                Notification.class
        );*/
        Optional<Notification> optional = notifRepo.findById(userId);
        if (!(optional.isPresent())) {
            throw new ChangeSetPersister.NotFoundException();
        }
        return optional.get();
    }

    public List<Notification> getNotification(){
        return notifRepo.findAll();
    }

   public Notification createNotification(Notification notification) {
        notification.setCreatedAt(new Date());
        return notifRepo.save(notification);

    }
}
