package com.service.event.service;

import com.service.event.domain.Notification;
import com.service.event.repository.NotificationStorgeRepository;
import lombok.var;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class PushNotificationService {

    private final NotificationStorgeRepository notificationStorageRepository;
    public PushNotificationService(NotificationStorgeRepository notificationStorageRepository) {
        this.notificationStorageRepository = notificationStorageRepository;
    }

    private List<Notification> getNotifs(Long userID) {
        var notifs = notificationStorageRepository.findByUserToIdAndDeliveredFalse(userID);
        notifs.forEach(x -> x.setDelivered(true));
        notificationStorageRepository.saveAll(notifs);
        return notifs;
    }

    public Flux<ServerSentEvent<List<Notification>>> getNotificationsByUserToID(Long userID) {

        if (userID != null && !userID.toString().trim().isEmpty()) {
            return Flux.interval(Duration.ofSeconds(1))
                    .onBackpressureDrop()
                    .log()
                    .publishOn(Schedulers.boundedElastic())
                    .map(sequence -> ServerSentEvent.<List<Notification>>builder().id(String.valueOf(sequence))
                            .event("user-list-event").data(getNotifs(userID))
                            .build());
        }

        return Flux.interval(Duration.ofSeconds(1))
                .onBackpressureDrop()
                .log()
                .map(sequence -> ServerSentEvent.<List<Notification>>builder()
                .id(String.valueOf(sequence)).event("user-list-event").data(new ArrayList<>()).build());
    }
}
