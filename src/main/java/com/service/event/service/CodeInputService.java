package com.service.event.service;

import com.service.event.domain.CodeInput;
import com.service.event.repository.CodeInputRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeInputService {
    @Autowired
    private CodeInputRepository codeRepo;
    public CodeInput addCode (CodeInput codeinput){
        return  codeRepo.save(codeinput);
    }
}
