package com.service.event.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notifications")
public class Notification {
    @MongoId(FieldType.OBJECT_ID)
    private String id;
    private String content;

    private Utilisateur userTo;
    private  Utilisateur userFrom;
    private Boolean delivered;
    private Boolean read=false;
    private  NotificationType notificationType;
    private Date createdAt;
}
