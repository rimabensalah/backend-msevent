package com.service.event.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventStats {
    //private LocalDateTime createdDate;
    private int year;
    private int month;
    private long validEventCount;
    private long totalEventCount;

}
