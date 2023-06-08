package com.service.event.repository;

import com.service.event.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EventRepository extends MongoRepository<Evenement,Long> {

    List<Evenement> findAllByTagsIn(Set<Tag> tags);
    @Query("{$group: {_id: { $month: '$createdDate'}, validEventCount: {$sum: {$cond: ['$isValide', 1, 0]}}, totalEventCount: {$sum: 1}}}")
    List<EventStats> getEventStatsByMonth();
    List<Evenement> findFirst4ByTagsIn(Set<Tag> tags);
    //First5
    Page<Evenement> findAllByTagsIn(Set<Tag> tags, Pageable pageable);
    Page<Evenement> findAllByTagsInAndUserId(Set<Tag> tags,Long id, Pageable pageable);
    Optional<Evenement> findEvenementById(Long id);

    List<Evenement> findFirst4ByOrderByCreatedDateDesc();
  // List<Evenement> findByCommentFromUserId(Long id);
    @Query(value="{ 'user.$id' : ?0 }")
    List<Evenement> findEvenementByUserId(Long id);
    Page<Evenement> findEvenementByUserId(Long id, Pageable pageable);
    List<Evenement> findTop5ByOrderByCreatedDateDesc();
    List<Evenement> findByCreatedDateGreaterThanEqualOrderByCreatedDateDesc(Date date);
    @Aggregation("{ $group: { id: '$user.id', count: { $sum: 1 } } }")
    List<UserPostCount> countEventsByUser();
    @Aggregation(pipeline = {
            "{$group: {_id: '$user', count: {$sum: 1}}}",
            "{$sort: {count: -1}}",
            "{$limit: 3}"
    })
    List<UserPostCount> findTop3UsersByPostCount();


    @Query("{ 'user.id' : ?0, 'createdDate' : { $gte: ?1, $lt: ?2 } }")
    List<Evenement> findByUserAndCreatedDateBetween(Long userId, Date start, Date end);
    //@Query("{ 'createdDate' : { $gte: ?1, $lt: ?2 } }")
    List<Evenement> findByCreatedDateBetween(Date start,Date end);
    List<Evenement> findAllByOrderByCreatedDateAsc();
    List<Evenement> findByCreatedDateBetweenAndIsValidate(Date start, Date end, boolean isValidate);
    List<Evenement> findByCreatedDateBetweenAndIsValidate (Date start, Date end);
    Page<Evenement> findByTitle(String title, Pageable pageable);
    Page<Evenement> findByTitleContainingIgnoreCase(String title, Pageable pageable);
   //@Query(value="{ 'tag.$tagName' : ?0 }")
    //OrUserId
   Page<Evenement> findByTitleContainingIgnoreCaseAndUserId(String title,Long id, Pageable pageable);
   Page<Evenement> findAllByTitleContainingIgnoreCase(String title,Pageable pageable);
    Page<Evenement> findByTitleContainingIgnoreCaseAndIsValidate(String title,Boolean isValidate, Pageable pageable);
    //@Query(value="{ 'isValidate' : ?1 }")
   Page<Evenement> findByIsValidate(Boolean isValidate,Pageable pageable);
   List<Evenement> findByUserAndIsValidate(Long userid,Boolean isValidate);
   Page<Evenement> findAllByOrderByCreatedDateDesc(Boolean isSorted,Pageable pageable);




}
