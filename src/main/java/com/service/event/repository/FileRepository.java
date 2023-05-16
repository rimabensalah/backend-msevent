package com.service.event.repository;

import com.service.event.domain.EventFile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileRepository extends MongoRepository<EventFile,String> {
}
