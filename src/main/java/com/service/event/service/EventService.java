package com.service.event.service;

import com.service.event.domain.*;

import com.service.event.payload.response.CommentRequest;
import com.service.event.payload.response.LikeRequest;
import com.service.event.repository.*;
import lombok.var;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;

import org.springframework.stereotype.Service;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepo;
    @Autowired
    private TagRepository tagRepo ;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private CommentRepository commentRepo;
    @Autowired
    private  TagService tagService;
    @Autowired
    SequenceGeneratorService sequenceGeneratorService;
    @Autowired
    public NotificationStorgeService notifService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private MongoTemplate mongoTemplate;
    private final MongoOperations mongoOperations;

    @Autowired
    private NotificationStorgeRepository notificationStorgeRepository;
    public EventService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }
   public Evenement saveEvent (Evenement event)
   {

       //Tag tagdata=tagRepo.save(tag);

       // event.setTag(tagdata);
       event.setId(sequenceGeneratorService.generateSequence(Evenement.SEQUENCE_NAME));
        event.setCreatedDate(LocalDateTime.now());
        event.setStatus(EventStatus.published);
        return eventRepo.save(event);
    }

    public boolean isUserContains(final List<Utilisateur> list, final String username) {
        return list.stream().anyMatch(o -> o.getUsername().equals(username));
    }

    //get event by id

    public Utilisateur getUserByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("user not found!"));
    }
    public Evenement  getEventById(Long id)
    {
        return eventRepo.findEvenementById(id).get();
    }

   public Utilisateur addBookmarked(Long id ,Long idUser){
       List<Evenement> bm = new ArrayList<>();
       Evenement eventData =eventRepo.findEvenementById(id).get();
       Utilisateur userData =userRepo.findById(idUser).get();
       if(userData.getBookmarked() == null){
           bm.add(eventData);
           userData.setBookmarked(bm);
       }else {
           userData.getBookmarked().add(eventData);
       }
       //userRepo.save(userData);

       return userRepo.save(userData);
   }



    //add comment to event
    public Evenement addComment(CommentRequest commentRequest)
    {
        Evenement event = getEventById(commentRequest.getEventId());
        Comment comment =commentRequest.getComment();
        List<Comment>  c = new ArrayList<Comment>();
        //comment user
        Utilisateur commentUser = userRepo.findByUsername(commentRequest.getComment().getFromUser().getUsername()).get();
        comment.setFromUser(commentUser);
        comment.setId(new ObjectId().toString());
        comment.setCreatedDate2(LocalDateTime.now());
        comment.setCan_like(true);
        comment.setCan_remove(true);
        comment.setUser_like(false);
        c.add(comment);

        //commentRepo.save(comment);
        //event user
        //Utilisateur eventUser = userRepo.findByUsername(event.)
        //event.setUser(eventUser);

       // if(event.getComments().isEmpty()){
            //event.setComments((Set<Comment>) comment);
            //event.getComments().add(comment);
       // }




        return  eventRepo.save(event);
    }
    //get comment by event id
    public List<Comment> getCommentsByEventId(Long id)
    {
        Evenement evenement=getEventById(id);
        return  evenement.getComments();
    }
    //add like
  public Comment addLike(
          Long eventid,
          String commentid,
          Utilisateur user
         // LikeRequest likeRequest
  ) throws IOException {
       Evenement eventData=eventRepo.findById(eventid).get();
       Comment comment = commentRepo.findById(commentid).get();
        Utilisateur commentFromUser = comment.getFromUser();
     // List<Utilisateur> likedUsers;
      List<Utilisateur>  lk = new ArrayList<Utilisateur>();
        // it's checking users at the same time

        Optional<Utilisateur> likedUser = userRepo.findByUsername(user.getUsername());

     if(comment.getLikedUsers() != null && isUserContains(comment.getLikedUsers(), user.getUsername())){
            //log.info("call remove like: " + likeRequest.getUser().getUsername());
         LikeRequest likeRequest=new LikeRequest(comment,user);
           return removeLike(likeRequest);
     }

     Integer nbrelike=comment.getLike_count();
       if(comment.getLike_count()==null){
           comment.setLike_count(1);
       }else{
           comment.setLike_count(nbrelike+1);
       }

       if(comment.getLikedUsers()==null){
           lk.add(likedUser.get());
           comment.setLikedUsers(lk);

       }else{
           comment.getLikedUsers().add(likedUser.get());
       }

      notifService.createNotificationStorage(Notification.builder()
              .delivered(false)
              .content("new like from " + comment.getFromUser().getUsername())
              .notificationType(NotificationType.like)
              .userFrom(user)
              .userTo(eventData.getUser())
              .createdAt(new Date()).build());

      // émettre l'événement de commentaire ajouté

        return commentRepo.save(comment);
    }

    public void addPostToTag(String tagId, long postId) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(tagId)));
        Tag tag = mongoTemplate.findOne(query, Tag.class);
        if (tag != null) {
            Evenement post = mongoTemplate.findById(postId, Evenement.class);
            if (post != null) {
                if(tag.getEvents_ids()==null){
                    tag.setEvents_ids(new HashSet<>());

                }else {
                    tag.getEvents_ids().add(post);
                }

                mongoTemplate.save(tag);
            }
        }
    }

    public Comment removeLike(LikeRequest likeRequest){
       Comment comment =commentRepo.findById(likeRequest.getComment().getId()).get();
        // it's checking users at the same time
        Utilisateur unLikedUser =userRepo.findByUsername(likeRequest.getUser().getUsername()).get();
        Integer nbrelike=comment.getLike_count();
       if(comment.getLike_count()==null){
            comment.setLike_count(0);
        }else{
            if(comment.getLike_count()<0){comment.setLike_count(0);}
            else{ comment.setLike_count(nbrelike-1);}

        }
        var updatedLikes = comment.getLikedUsers()
                .stream()
                .filter(x -> !x.getUsername().equals(unLikedUser.getUsername()))
                .collect(Collectors.toList());
        comment.setLikedUsers(updatedLikes);
        return commentRepo.save(comment);

    }

    public Comment addVote(String idcomment,Utilisateur user)
    {
        Comment comment = commentRepo.findById(idcomment).get();
        Utilisateur commentFromUser = comment.getFromUser();
        // List<Utilisateur> likedUsers;
        List<Utilisateur>  vote = new ArrayList<Utilisateur>();
        // it's checking users at the same time
        Utilisateur votedUser = userRepo.findByUsername(user.getUsername()).get();
        if(comment.getVoteUsers() != null && isUserContains(comment.getVoteUsers(), user.getUsername())){
            return removeVote(idcomment,user);
        }

        if(comment.getVoteUsers()==null){
            vote.add(votedUser);
            comment.setVoteUsers(vote);

        }else{
            comment.getVoteUsers().add(votedUser);
        }


        return commentRepo.save(comment);
    }

    public Comment removeVote(String idcomment,Utilisateur user)
    {
        Comment comment = commentRepo.findById(idcomment).get();
        //Utilisateur commentFromUser = comment.getFromUser();
        // List<Utilisateur> likedUsers;
        //List<Utilisateur>  vote = new ArrayList<Utilisateur>();
        Utilisateur unVoteUser =userRepo.findByUsername(user.getUsername()).get();
        var updatedVotes = comment.getVoteUsers()
                .stream()
                .filter(x -> !x.getUsername().equals(unVoteUser.getUsername()))
                .collect(Collectors.toList());
        comment.setVoteUsers(updatedVotes);
        return commentRepo.save(comment);
    }

    public Comment validateComment(String idcomment){
        Comment comment = commentRepo.findById(idcomment).get();
        comment.setValidateComment(true);
        return commentRepo.save(comment);
    }
    public Evenement validate (Long eventid, String commentid){
        Comment comment = commentRepo.findById(commentid).get();
        Evenement evenementData =eventRepo.findEvenementById(eventid).get();
        evenementData.setIsValidate(true);
        comment.setValidateComment(true);
        eventRepo.save(evenementData);
        commentRepo.save(comment);


        return eventRepo.findEvenementById(eventid).get();
    }

    public Evenement invalidComment (Long eventid,String commentid){
        Comment comment = commentRepo.findById(commentid).get();
        Evenement evenementData =eventRepo.findEvenementById(eventid).get();
        evenementData.setIsValidate(false);
        comment.setValidateComment(false);
        eventRepo.save(evenementData);
        commentRepo.save(comment);


        return eventRepo.findEvenementById(eventid).get();
    }

    public List<Evenement> getAll() {
        return eventRepo.findAll();
    }

    public  Long totalEventBuUser (Long id){
       Long totalEvent=eventRepo.findEvenementByUserId(id).stream().count();
       return totalEvent ;

    }
    public Long validatedEventByUser(Long userid){
        List<Evenement> validatedevent=eventRepo.findByUserAndIsValidate(userid,true);
        Long totalValidatedEvent=validatedevent.stream().count();
        return totalValidatedEvent;
    }
    public Long totalEvent(){
        Long totalevent=eventRepo.findAll().stream().count();
        return totalevent;
    }
    public Long totalComments(){
        Long totalComment=commentRepo.findAll().stream().count();
        return totalComment;
    }
    public Long countTotalTag(){
        Long totaltag=tagRepo.findAll().stream().count();
        return totaltag;
    }
    public Long totalCommentByUser(Long userid){
       Long totalComment=commentRepo.findCommentByFromUserId(userid).stream().count();
       return  totalComment;
    }
    public List<Evenement> getRecentEvent(){
       return eventRepo.findFirst4ByOrderByCreatedDateDesc();
    }
    public List<Comment> eventUserAttribute(Long id) {
        return commentRepo.findCommentByFromUserId(id);
    }
    public Optional<Evenement> getById(Long id){return eventRepo.findEvenementById(id); }
    public Flux<ServerSentEvent<List<Evenement>>> streamPosts() {
        return Flux.interval(Duration.ofSeconds(1))
                .onBackpressureDrop()
                .log()
                .publishOn(Schedulers.boundedElastic())
                .map(sequence -> ServerSentEvent.<List<Evenement>>builder().id(String.valueOf(sequence))
                        .event("post-list-event").data(getAll())
                        .build());

    }

    public List<Comment> findAnswersByPostId(Long postId) {
        Query query = new Query(Criteria.where("id").is(postId));
        Evenement post = mongoTemplate.findOne(query, Evenement.class);
        return post.getComments();
    }


    public List<Evenement> findPostsByContributor(
            Long userId,
            int pageNumber, int pageSize
    ) {

            Page<Evenement> pageTuts;
            List<Evenement> events = new ArrayList<Evenement>();
           // Pageable paging = PageRequest.of(pageNumber, pageSize);


            Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
            Page<Evenement> allPosts = eventRepo.findEvenementByUserId(userId, pageable);
            List<Evenement> userPosts = new ArrayList<>();
            // Page<Evenement> pageTuts;
            for (Evenement post : allPosts) {
                List<Comment> comments = post.getComments();
                if (comments == null || comments.isEmpty()) {
                    continue;
                }
                for (Comment comment : post.getComments()) {
                    if (comment.getFromUser().getId().equals(userId)) {
                        userPosts.add(post);
                        break;
                    }
                }
            }
            return userPosts;

    }


    public List<Evenement> findPopularEvent() {
        List<Evenement> popularPosts = eventRepo.findAll();
        // Trier les events  en fonction du nombre de commentaires
        Collections.sort(popularPosts, new Comparator<Evenement>() {

            @Override
            public int compare(Evenement post1, Evenement post2) {
                List<Comment> comments1 = post1.getComments();
                List<Comment> comments2 = post2.getComments();
                if (comments1 == null) {
                    comments1 = new ArrayList<>();
                }
                if (comments2 == null) {
                    comments2 = new ArrayList<>();
                }
                return Integer.compare(comments2.size(), comments1.size());
            }
        });
        return popularPosts;
    }

    public List<EventMonthlyData> countEventEveryMonth(){
        Aggregation aggregation = newAggregation(
                group("createdDate")
                        .count().as("count")
        );
        AggregationResults<EventMonthlyData> results = mongoTemplate.aggregate(aggregation, Evenement.class, EventMonthlyData.class);
        return results.getMappedResults();
    }
    public List<EventMonthlyData> groupByCreatedDate() {
        TypedAggregation<Evenement> aggregation = newAggregation(Evenement.class,
                Aggregation.project("createdDate")
                        .andExpression("year(createdDate)").as("year")
                        .andExpression("month(createdDate)").as("month")
                        .and("title").as("title"),
                group("year", "month")
                        .count().as("count")
                        .first("year").as("year")
                        .first("month").as("month")
                        .last("createdDate").as("createdDate")
                        .addToSet("title").as("titles")
        );
        AggregationResults<EventMonthlyData> results = mongoTemplate.aggregate(aggregation, EventMonthlyData.class);
        return results.getMappedResults();
    }

    public List<EventMonthlyData> groupByCreatedDateUser(Long userid) {
        TypedAggregation<Evenement> aggregation = newAggregation(Evenement.class,
                match(Criteria.where("user").is(userid)),
                project("createdDate", "title")
                        .andExpression("year(createdDate)").as("year")
                        .andExpression("month(createdDate)").as("month"),
                group("year", "month")
                        .count().as("count")
                        .first("year").as("year")
                        .first("month").as("month"),
             /*   project("year", "month", "count")
                        .and(ConvertOperators.ToString.valueOf("month")).as("monthString")
                        .and(ConvertOperators.ToString.valueOf("year")).as("yearString")
                        .andExpression("concat(monthString, '-', yearString)").as("monthYear"),*/
                sort(Sort.Direction.ASC, "year", "month")
        );
        AggregationResults<EventMonthlyData> results = mongoTemplate.aggregate(aggregation, EventMonthlyData.class);
        return results.getMappedResults();
    }

    public List<Document> groupByTag(){

        Aggregation aggregation = Aggregation.newAggregation(
                unwind("tags"),
                group("tags.tagName").count().as("count")
        );
            AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "events", Document.class);
            return result.getMappedResults();


    }

    public Map<String, Object> displayUserStats(Long id) {
        // récupérer tous les utilisateurs
        Map<String, Object> stats = new HashMap<>();

        //List<User> users = userRepository.findAll();

        // afficher les statistiques d'utilisation pour chaque utilisateur
        //for (User user : users) {
            long numQuestions = totalEventBuUser(id);
            int  eventMonhth=countPostsThisMonthByContributor(id);
            long numAnswers = totalCommentByUser(id);
            int commentMonth=getNumberOfCommentsAddedThisMonth(id);
            long validatedEvent=validatedEventByUser(id);
            List<Evenement> popularevent=findPopularEvent();
            List<EventMonthlyData> countEventEveryMonth=groupByCreatedDate();
            List<EventMonthlyData> countEventEveryMonthuser=groupByCreatedDateUser(id);
            List<Document> countEventByTag=groupByTag();
           // List<Evenement> eventbycontibutor=findPostsByContributor(id);
            //long numVotes = voteRepository.countByUser(user);
            stats.put("numQuestions",numQuestions);
            stats.put("eventMonth",eventMonhth);
            stats.put("numAnswers",numAnswers);
            stats.put("commentMonth",commentMonth);
            stats.put("validatedEvent",validatedEvent);
            stats.put("popularevent",popularevent);
            stats.put("countEventEveryMonth",countEventEveryMonth);
            stats.put("countEventEveryMonthuser",countEventEveryMonthuser);
            stats.put("countEventByTag",countEventByTag);
            System.out.println("Statistiques d'utilisation pour l'utilisateur " + userRepo.findById(id).get().getUsername() + ":");
            System.out.println("- Nombre de questions posées : " + numQuestions);
            System.out.println("- Nombre de réponses apportées : " + numAnswers);

            System.out.println();

        return stats;
    }
    public int countPostsThisMonthByContributor(Long contributorId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate startOfNextMonth = now.plusMonths(1).withDayOfMonth(1);
        Date start = Date.from(startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(startOfNextMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Evenement> posts = eventRepo.findByUserAndCreatedDateBetween(contributorId, start, end);
        return posts.size();
    }
    public int countPostsThisMonth() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate startOfNextMonth = now.plusMonths(1).withDayOfMonth(1);
        Date start = Date.from(startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(startOfNextMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Evenement> posts = eventRepo.findByCreatedDateBetween( start, end);
        return posts.size();
    }
    public int getNumberOfCommentsAddedThisMonth(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = LocalDateTime.of(now.getYear(), now.getMonth(), 1, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

        List<Comment> userComments = commentRepo.findByFromUserIdAndCreatedDate2Between(userId, startOfMonth, endOfMonth);

        return userComments.size();
    }
    public int NumberOfCommentsAddedThisMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = LocalDateTime.of(now.getYear(), now.getMonth(), 1, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

        List<Comment> userComments = commentRepo.findByCreatedDate2Between( startOfMonth, endOfMonth);

        return userComments.size();
    }
    public List<UserPostCount> findTop3UsersByPostCount() {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.group("user").count().as("count"),
                Aggregation.sort(Sort.Direction.DESC, "count"),
                Aggregation.limit(3)
        );
        AggregationResults<UserPostCount> results = mongoTemplate.aggregate(agg, "events", UserPostCount.class);
        return results.getMappedResults();
    }

    public Map<String,Object> displayAdminStats(){
        Map<String, Object> stats = new HashMap<>();
        long numQuestions = totalEvent();
        int  eventMonhth=countPostsThisMonth();
        long numAnswers = totalComments();
        int commentMonth=NumberOfCommentsAddedThisMonth();
        long numTags=countTotalTag();
        List<EventMonthlyData> countEventEveryMonth=groupByCreatedDate();
        List<Evenement> recentEvents=eventRepo.findTop5ByOrderByCreatedDateDesc();
        List<Notification> recentActivity=notificationStorgeRepository.findTop5ByOrderByCreatedAtDesc();
        stats.put("numQuestions",numQuestions);
        stats.put("eventMonth",eventMonhth);
        stats.put("numAnswers",numAnswers);
        stats.put("commentMonth",commentMonth);
        stats.put("totalTag",numTags);
        stats.put("recentEvents",recentEvents);
        stats.put("recentActivity",recentActivity);

        System.out.println("Statistiques d'utilisation pour l'admin " +  ":");
        System.out.println("- Nombre de questions posées : " + numQuestions);
        System.out.println("- Nombre de questions posées par month : " + eventMonhth);
        System.out.println("- Nombre de réponses apportées : " + numAnswers);
        System.out.println("- Nombre de réponses apportées per month: " + commentMonth);
        System.out.println("- Nombre de tags apportées  " + numTags);
        stats.put("countEventEveryMonth",countEventEveryMonth);
        System.out.println();

        return stats;
    }


}
