package com.service.event.api;

import com.service.event.domain.TagModel;
import com.service.event.payload.response.MessageReponse;
import com.service.event.repository.TagRepository;
import com.service.event.service.EventService;
import com.service.event.service.TagService;
import com.service.event.domain.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api/tag")
public class TagController {
    @Autowired
    TagService tagService;
    @Autowired
    private TagRepository tagRepo ;
    @Autowired
    private EventService eventService;
    //http://localhost:8088/api/tag/addtag

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/tags/count")
    public List<TagModel> countPostsByTag() {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.unwind("tags"),
                Aggregation.group("tags.tagName").count().as("count"),
                Aggregation.project("count").and("tagName").previousOperation()
        );
        AggregationResults<TagModel> results = mongoTemplate.aggregate(agg, "events", TagModel.class);
        return results.getMappedResults();
    }


    @GetMapping("/postCount")
    public ResponseEntity<Map<String, Integer>> getPostCountByTag() {
        Map<String, Integer> postCountByTag = tagService.getPostCountByTag();
        return ResponseEntity.ok(postCountByTag);
    }
    @GetMapping("/Count2")
    public Map<String, Map<String, Integer>> getPostCountByTag2() {
        return tagService.getPostCountByTag2();
    }
    @PostMapping("/addtag")
    public ResponseEntity<?> addTag(@RequestBody Tag tag){
        if(tagRepo.existsByTagName(tag.getTagName()))
        {return ResponseEntity.badRequest().body(new MessageReponse("Error: tagname is already taken!"));
        }else{
            return  new ResponseEntity<>(tagService.saveTag(tag), HttpStatus.ACCEPTED);
        }
    }
    //http://localhost:8088/api/tag/alltags
    @GetMapping("/alltags")
    public ResponseEntity<List<Tag>> getAllTag(@RequestParam(required = false) String name){
        try{
            List<Tag> tags= new ArrayList<Tag>();
            if(name == null){
                tagRepo.findAll().forEach(tags::add);
            }
            /*else{
                tagRepo.findByTagName(name).forEach(tags::add);
            }*/

            if(tags.isEmpty()){
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(tags,HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/tag/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable("id") Long id){
        Optional<Tag> tagData=tagRepo.findById(id);
        if(tagData.isPresent())
            return new ResponseEntity<>(tagData.get(),HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    //get tag by tagName
    @GetMapping("/gettag/{name}")
    public ResponseEntity<Tag> getTagByname(@PathVariable("name") String name){
        Optional<Tag> tagData=tagRepo.findByTagName(name);
        if(tagData.isPresent())
            return new ResponseEntity<>(tagData.get(),HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    //create list of tags
    @PostMapping("/tags")
    public ResponseEntity<?> createTags(@RequestBody List<Tag> createTagtoList){

        List<Tag> tags = new ArrayList<>();
        for (Tag tag : createTagtoList) {
            String tagName = tag.getTagName();
            if(tagRepo.existsByTagName(tagName)){
                return ResponseEntity.badRequest().body( new MessageReponse("Error: tagname is already taken!"));
            }
            Tag tagdata= new Tag();
            tagdata.setTagName(tagName);
            tags.add(tagdata);
        }
        List<Tag> tagsCreated= tagRepo.saveAll(tags);
        return  new ResponseEntity<>(tagsCreated, HttpStatus.CREATED);

    }








}
