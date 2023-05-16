package com.service.event.payload.response;

import com.service.event.domain.Comment;
import com.service.event.domain.Evenement;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentRequest {
    private Long eventId;
    private Comment comment;
}
