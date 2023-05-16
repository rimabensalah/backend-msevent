package com.service.event.service;

import com.service.event.domain.Evenement;
import com.service.event.repository.EventRepository;
import com.service.event.repository.TagRepository;
import com.service.event.domain.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TagService {
    @Autowired
    private TagRepository tagRepo;
    @Autowired
    private EventRepository postRepository;

    public Map<String, Integer> getPostCountByTag() {
        Map<String, Integer> postCountByTag = new HashMap<>();

        List<Evenement> posts = postRepository.findAll();
        for (Evenement post : posts) {
            for (Tag tag : post.getTags()) {
                String tagName = tag.getTagName();
                int postCount = postCountByTag.getOrDefault(tagName, 0);
                postCountByTag.put(tagName, postCount + 1);
            }
        }

        return postCountByTag;
    }
    //add new tag
    public Tag saveTag(Tag tag){
        return tagRepo.save(tag);
    }

    public Tag getTagByTagName(String tagName) {
        return tagRepo.findByTagName(tagName)
                .orElseThrow(() -> new RuntimeException("tag not found!"));
    }
}
