package com.service.event.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user")
public class Utilisateur {
    @Id
    private Long id;

    private String username;

    private String email;
    private  String userImage;
    private String userRole;

    @DBRef
   @JsonIgnore
    private List<Evenement> bookmarked;

    private  List<Long> bookmarkedId;

}
