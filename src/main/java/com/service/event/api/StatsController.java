package com.service.event.api;
import com.service.event.service.EventService;
import org.bson.Document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.data.mongodb.core.aggregation.*;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.DateToString.dateToString;


@RestController
@RequestMapping("/api/stat")
public class StatsController {
    private final MongoTemplate mongoTemplate;
    @Autowired
    private EventService eventService;

    public StatsController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/getuserdashstat/{id}")
    public Map<String, Object> getStats(@PathVariable("id") Long id) {
        return eventService.displayUserStats(id);
    }

    @GetMapping("/getadminsashstat")
    public Map<String,Object> getAdminStats(){
        return eventService.displayAdminStats();
    }
}
