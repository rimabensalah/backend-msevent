package com.service.event.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.Binary;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "eventfile")
public class EventFile {
    @MongoId(FieldType.OBJECT_ID)
    private String id;
    private String filename;
    //private String fileType;
    //private String fileSize;
    private Binary file;

    public EventFile(String filename) {
        this.filename = filename;
    }

}
