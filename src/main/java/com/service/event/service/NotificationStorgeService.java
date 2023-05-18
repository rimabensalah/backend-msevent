package com.service.event.service;

import com.service.event.domain.Notification;
import com.service.event.repository.NotificationStorgeRepository;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailTo;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class NotificationStorgeService {

    @Autowired
    private JavaMailSender javaMailSender;




    private final NotificationStorgeRepository notifRepository;

    public NotificationStorgeService (NotificationStorgeRepository notifRepository) {
        this.notifRepository = notifRepository;
    }

    public Notification createNotificationStorage(Notification notificationStorage) {
        return notifRepository.save(notificationStorage);
    }

    public Notification getNotificationById(String id){
        return notifRepository.findById(id).orElseThrow(() -> new RuntimeException("notification not found!"));
    }
    public List<Notification> getNotificationsByUserIDNotRead(Long userID) {
        return notifRepository.findByUserToIdAndDeliveredFalse(userID);
    }
    public Page<Notification> getAllNotification(Pageable pageable){
        return notifRepository.findAll(pageable);
    }
    public List<Notification> getNotificationsByUserID(Long userID) {
        return notifRepository.findByUserToIdOrderByCreatedAtDesc(userID);
    }
    public Notification changeNotifStatusToRead(String notifID) {
        var notif = notifRepository.findById(notifID)
                .orElseThrow(() -> new RuntimeException("not found!"));
        notif.setRead(true);
        return notifRepository.save(notif);
    }
    public List<Notification> makeAllNotifsRead(Long userID) {
        var notifs = notifRepository.findByUserToIdAndReadFalse(userID);
        notifs.forEach(x -> x.setRead(true));
        notifRepository.saveAll(notifs);
        return notifs;
    }

   /* public List<Notification> getnotificationBycreatedate(Date createAt){
        if(createAt==null){
            return notifRepository.findAll();
        }else {
            return  notifRepository.findByCreatedAt(createAt);
        }

    }*/

    public void clear() {
        notifRepository.deleteAll();
    }

    public void clearByid(String notifID){
        notifRepository.deleteById(notifID);
    }

    public void sendNotification(String recipient, String subject, String message)
            throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setTo(recipient);
        helper.setSubject(subject);
        helper.setText(message, true);

        javaMailSender.send(mimeMessage);
    }



}
