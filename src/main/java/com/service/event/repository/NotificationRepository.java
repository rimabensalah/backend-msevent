package com.service.event.repository;

import com.service.event.domain.Comment;
import com.service.event.domain.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository  extends MongoRepository<Notification,String> {
}
