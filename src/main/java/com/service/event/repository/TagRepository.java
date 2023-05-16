package com.service.event.repository;

import com.service.event.domain.Evenement;
import com.service.event.domain.Tag;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends MongoRepository <Tag,Long>{
   Optional<Tag> findByTagName (String tagName);
    Boolean existsByTagName (String tagName);

    Optional<Tag> findById(String tagId);
}
