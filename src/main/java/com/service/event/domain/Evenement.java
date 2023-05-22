package com.service.event.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "events")
public class Evenement {

    @Transient
    public static final String SEQUENCE_NAME = "events_sequence";
    @Id
    private Long id;
    private String title;
    private String content;
    //private  String code ;
    private EventStatus status;


    //@Field(value = "created_date")
    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @DBRef
    //private Tag tag;
    private Set<Tag> tags;


    @DBRef
    private EventFile eventfile;

    @DBRef
    private Utilisateur user;

    @DBRef
    private List<Comment> comments;
    private Boolean isValidate = false;

    public Evenement(Long id , String title ,String content ,LocalDateTime createdDate ) {
        this.id = id;
        this.title=title;
        this.content=content;
        this.createdDate=createdDate;
    }
}
