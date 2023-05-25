package com.service.event.repository;

import com.service.event.domain.Evenement;
import com.service.event.domain.Utilisateur;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<Utilisateur,Long> {
    Optional <Utilisateur> findByUsername(String username);
    Optional<Utilisateur> findByEmail(String email);
}
