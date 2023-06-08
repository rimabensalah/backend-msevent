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

        List<Tag> allTags = tagRepo.findAll();
        for (Tag tag : allTags) {
            String tagName = tag.getTagName();
            postCountByTag.putIfAbsent(tagName, 0);
        }

        return postCountByTag;
    }

    public Map<String, Map<String, Integer>> getPostCountByTag2() {
        Map<String, Map<String, Integer>> tagInfoMap = new HashMap<>();

        List<Evenement> posts = postRepository.findAll();

        for (Evenement post : posts) {
            for (Tag tag : post.getTags()) {
                String tagId = tag.getId();
                String tagName = tag.getTagName();

                if (!tagInfoMap.containsKey(tagId)) {
                    tagInfoMap.put(tagId, new HashMap<>());
                }

                Map<String, Integer> tagInfo = tagInfoMap.get(tagId);
                int postCount = tagInfo.getOrDefault(tagName, 0);
                tagInfo.put(tagName, postCount + 1);
            }
        }

        List<Tag> allTags = tagRepo.findAll();
        for (Tag tag : allTags) {
            String tagId = tag.getId();
            String tagName = tag.getTagName();

            if (!tagInfoMap.containsKey(tagId)) {
                tagInfoMap.put(tagId, new HashMap<>());
            }

            Map<String, Integer> tagInfo = tagInfoMap.get(tagId);
            tagInfo.putIfAbsent(tagName, 0);
        }

        return tagInfoMap;
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
