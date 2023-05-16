package com.service.event.repository;

import com.service.event.domain.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationStorgeRepository extends MongoRepository<Notification,String> {
    Optional<Notification> findById(String id);
    List<Notification> findTop5ByOrderByCreatedAtDesc();

    List<Notification> findByUserToIdOrderByCreatedAtDesc(Long id);

    @Query
    List<Notification> findTop5ByUserToId(Long id);

    List<Notification> findByUserToIdAndDeliveredFalse(Long id);
    List<Notification> findByUserToIdAndReadFalse(Long id);
    List<Notification> findTop5ByUserToIdOrderByCreatedAt(Long id);
    List<Notification> findFirst5ByUserToIdOrderByCreatedAtDesc(Long id);

}
