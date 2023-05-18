package com.service.event.api;

import com.service.event.domain.Notification;
import com.service.event.repository.NotificationRepository;
import com.service.event.repository.NotificationStorgeRepository;
import com.service.event.service.NotificationStorgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true")
@RequestMapping("/notification")
@RestController
public class NotificationStorageController {
    private final NotificationStorgeService notifService;
    @Autowired
    private NotificationStorgeRepository notifRepo;


    public NotificationStorageController(NotificationStorgeService notifService) {
        this.notifService = notifService;
    }

    @GetMapping("/{userID}")
    public ResponseEntity<List<Notification>> getNotificationsByUserID(@PathVariable Long userID) {
        return ResponseEntity.ok(notifService.getNotificationsByUserID(userID));
    }

    @GetMapping("/notificationhistory")
    public ResponseEntity<?> getNotificationHistory(
           // @RequestParam(defaultValue = "0") int page,
           // @RequestParam(defaultValue = "10") int size
           @RequestParam(defaultValue = "1") int page,
           @PageableDefault(size = 10) Pageable pageable
    ) {
        pageable = PageRequest.of(page, pageable.getPageSize(), pageable.getSort());
        return ResponseEntity.ok(notifService.getAllNotification(pageable));
    }


    @PatchMapping("/read/{notifID}")
    public ResponseEntity changeNotifStatusToRead(@PathVariable String notifID) {
        return ResponseEntity.ok(notifService.changeNotifStatusToRead(notifID));
    }
    @PatchMapping("allread/{userID}")
    public ResponseEntity changeNotifStatusToAllRead(@PathVariable Long userID) {
        return ResponseEntity.ok(notifService.makeAllNotifsRead(userID));
    }
    @GetMapping("/lastnotif/{userID}")
    public ResponseEntity<List<Notification>> getNotificationsByDate(@PathVariable Long userID) {
        return ResponseEntity.ok(notifRepo.findFirst5ByUserToIdOrderByCreatedAtDesc(userID));
    }

    @DeleteMapping("/clear")
    public void clearNotif() {
         notifService.clear();
    }

    @DeleteMapping("/clearbyid/{notifID}")
    public void clearNotifbyid(@PathVariable String notifID) {
        notifService.clearByid(notifID);
    }

    @GetMapping("/findbycreateddate")
    public Page<Notification> getNotificationsByCreatedAt(
            @RequestParam(value = "createdAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAt,
            @RequestParam(defaultValue = "1") int page,
            @PageableDefault(size = 10) Pageable pageable) {
        pageable = PageRequest.of(page, pageable.getPageSize(), pageable.getSort());

        if(createdAt==null){return notifService.getAllNotification(pageable);}
        else{
            Date startDate = Date.from(createdAt.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Adding 1 day to include notifications until the end of the day
            Date endDate = calendar.getTime();
            return notifRepo.findByCreatedAt(startDate, endDate,pageable);
        }

    }



}
