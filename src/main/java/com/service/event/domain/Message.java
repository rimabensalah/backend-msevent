package com.service.event.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    /*private String senderName;
    private String receiverName;
    private String message;
    private Status status;*/

    private String text;

    private String to;
    private String from;
}
