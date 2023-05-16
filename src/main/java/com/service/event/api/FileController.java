package com.service.event.api;

import com.service.event.domain.Comment;
import com.service.event.domain.Evenement;
import com.service.event.domain.EventFile;
import com.service.event.payload.response.LikeRequest;
import com.service.event.repository.FileRepository;
import com.service.event.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@CrossOrigin("http://localhost:3000/")
@RestController
@RequestMapping("/api/file")
public class FileController {
    @Autowired
    FileService fileService;
    @Autowired
    private FileRepository fileRepo;

    //upload file
    /*@PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        return new ResponseEntity<>(fileService.addFile(file), HttpStatus.OK);
    }*/
    @PostMapping("/photos/add")
    public ResponseEntity<?> addPhoto(@RequestParam("title") String title,
                           @RequestParam("image") MultipartFile image, Model model)throws IOException
    {
        String id = fileService.addFile(title, image);
        return new ResponseEntity<>(fileRepo.findById(id),HttpStatus.OK);
    }

    //get Allimage
    @GetMapping("/photos")
    public ResponseEntity<List<EventFile>> getAllEvent(@RequestParam(required = false) String name)
    {
        try{
            List<EventFile> files= new ArrayList<EventFile>();
            if(name == null){
                fileRepo.findAll().forEach(files::add);
            }


            if(files.isEmpty()){
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(files,HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //get image par id
    @GetMapping("/photo/{id}")
    public String getPhoto(@PathVariable String id, Model model) {
        EventFile photo = fileService.getPhoto(id);
        model.addAttribute("title", photo.getFile());
        model.addAttribute("image",
                Base64.getEncoder().encodeToString(photo.getFile().getData()));
        return "photo";
    }
    //add like


}
