package com.service.event.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment {
    @MongoId(FieldType.OBJECT_ID)
    private String id;

    private String content;

    private String oldContent;
    @CreatedDate
    private LocalDateTime createdDate2;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    private Utilisateur fromUser;
    //nbre de like
    private Integer like_count;
    //si le lecteur a aimé le commentaire
    private Boolean user_like;
    private List<Utilisateur> likedUsers;
    private List<Utilisateur> voteUsers;
    private Boolean can_like;
    private Boolean can_remove;

    private List<Comment> comments ;

    private Boolean validateComment;

    @DBRef
    private EventFile eventfile;

}
