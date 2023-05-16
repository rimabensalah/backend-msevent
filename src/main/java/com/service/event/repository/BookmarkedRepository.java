package com.service.event.repository;

import com.service.event.domain.BookmarkedEvent;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkedRepository extends MongoRepository<BookmarkedEvent,String> {
    List<BookmarkedEvent> findAllBookmarkedEventByUserId(Long id);
}
