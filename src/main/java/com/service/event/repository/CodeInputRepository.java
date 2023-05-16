package com.service.event.repository;

import com.service.event.domain.CodeInput;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeInputRepository  extends MongoRepository<CodeInput,String> {
}
