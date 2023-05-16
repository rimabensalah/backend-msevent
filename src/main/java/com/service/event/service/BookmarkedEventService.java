package com.service.event.service;

import com.service.event.domain.BookmarkedEvent;
import com.service.event.domain.Evenement;
import com.service.event.domain.Utilisateur;
import com.service.event.repository.BookmarkedRepository;
import com.service.event.repository.EventRepository;
import com.service.event.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookmarkedEventService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepo;
   @Autowired
   private BookmarkedRepository bookmarkedRepository;
    public BookmarkedEvent addBookmarked(BookmarkedEvent bookmarkedEvent){

        Utilisateur user =userRepository.findById(bookmarkedEvent.getUser().getId()).get();
        Evenement event =eventRepo.findEvenementById(bookmarkedEvent.getEvent().getId()).get();
        event.getEventfile().setFile(null);
        //bookmarked.getEvent().setEventfile(null);
        BookmarkedEvent bookmarked =new BookmarkedEvent(user,event);
       // bookmarked.getEvent().setCreatedDate(null);
        return  bookmarkedRepository.save(bookmarked);
    }

    public List<?> findBookmarkedByuser (Long id){


        return bookmarkedRepository.findAllBookmarkedEventByUserId(id);
    }
}
