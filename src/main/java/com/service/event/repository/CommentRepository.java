package com.service.event.repository;

import com.service.event.domain.Comment;
import com.service.event.domain.Evenement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment,String> {

    List<Comment> findCommentByFromUserId(Long userid);
    List<Comment> findByFromUserIdAndCreatedDate2Between(Long userId, LocalDateTime start, LocalDateTime end);
    List<Comment> findByCreatedDate2Between(LocalDateTime start,LocalDateTime end);
}
