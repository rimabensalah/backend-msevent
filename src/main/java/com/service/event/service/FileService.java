package com.service.event.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.service.event.domain.EventFile;
import com.service.event.repository.EventRepository;
import com.service.event.repository.FileRepository;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileService {
    @Autowired
    private GridFsTemplate template;

    @Autowired
    private GridFsOperations operations;
    @Autowired
    private FileRepository fileRepo;


    public String addFile(String filename, MultipartFile file) throws IOException {
        EventFile photo = new EventFile(filename);
        photo.setFile(new Binary(BsonBinarySubType.BINARY, file.getBytes()));
        photo = fileRepo.insert(photo);
        return photo.getId();
    }

    //get image
    public EventFile getPhoto (String id) {
        return fileRepo.findById(id).get();
    }



}
