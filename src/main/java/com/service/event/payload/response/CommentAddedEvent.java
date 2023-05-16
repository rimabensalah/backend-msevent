package com.service.event.payload.response;

import org.springframework.context.ApplicationEvent;

public class CommentAddedEvent extends ApplicationEvent {
    private Long postId;
    private Long userId;

    public CommentAddedEvent(Object source, Long postId, Long userId) {
        super(source);
        this.postId = postId;
        this.userId = userId;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getUserId() {
        return userId;
    }
}
