package com.service.event.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.service.event.domain.Comment;
import com.service.event.domain.Evenement;
import com.service.event.domain.Utilisateur;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeRequest {
    private Comment comment;
    private Utilisateur user;
}
