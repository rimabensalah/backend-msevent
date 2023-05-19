package com.service.event.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.event.domain.CodeInput;
import com.service.event.repository.CodeInputRepository;
import com.service.event.service.CodeInputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@CrossOrigin("http://localhost:3000")
@RestController
@RequestMapping("/api/codeinput")
public class CodeInputController {
    @Autowired
    CodeInputService codeInputService;
    @Autowired
    private CodeInputRepository codeInputRepository;

    @PostMapping
    public ResponseEntity<String> saveCode(@RequestBody Map<String, Object> body) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(body);
            CodeInput code = objectMapper.readValue(json, CodeInput.class);
            codeInputService.addCode(code);
            return ResponseEntity.ok("Code saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving code: " + e.getMessage());
        }
    }


    @GetMapping("/getcodebyid/{idcode}")
    public ResponseEntity<CodeInput> getcodebyid(@PathVariable("id") String id){
        Optional<CodeInput> codeData=codeInputRepository.findById(id);
        if(codeData.isPresent()){
            return new ResponseEntity<>(codeData.get(),HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }
}
