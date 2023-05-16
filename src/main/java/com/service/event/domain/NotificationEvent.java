package com.service.event.domain;

public class NotificationEvent {
    private final Notification notification;

    public NotificationEvent(Notification notification) {
        this.notification = notification;
    }

    public Notification getNotification() {
        return notification;
    }
}
