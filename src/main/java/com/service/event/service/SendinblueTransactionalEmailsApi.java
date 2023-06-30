package com.service.event.service;

import org.springframework.stereotype.Service;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailTo;
import sibModel.SendSmtpEmailSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
public class SendinblueTransactionalEmailsApi {

    public void sendMail(String subject, String recepientName, String recepientEmail, String content) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        // Configure API key authorization: api-key
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey("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");

        try {
            TransactionalEmailsApi api = new TransactionalEmailsApi();
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail("rymabnslh@gmail.com");
            sender.setName("Ryma");
            List<SendSmtpEmailTo> toList = new ArrayList<>();
            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(recepientEmail); // to make dynamic
            to.setName(recepientName); // to make dynamic
            toList.add(to);

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(toList);
           // sendSmtpEmail.setTemplateId(1L);
            sendSmtpEmail.setHtmlContent("<html><body>" + content + "</body></html>");
            sendSmtpEmail.setSubject(subject);

            CreateSmtpEmail response = api.sendTransacEmail(sendSmtpEmail);
            System.out.println(response.toString());
        } catch (Exception e) {
            System.out.println("Exception occurred:- " + e.getMessage());
            e.printStackTrace();
        }
    }

   /* public void sendCommentNotification(String toEmail,String commentText){
        try {
            ApiClient defaultClient = Configuration.getDefaultApiClient();
            defaultClient.setApiKey("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");

            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(defaultClient);

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            List<SendSmtpEmailTo> toList = new ArrayList<SendSmtpEmailTo>();

            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(toEmail);
            toList.add(to);
            sendSmtpEmail.setTo(toList);

            sendSmtpEmail.setTemplateId(1L);
            sendSmtpEmail.setParams(new HashMap<String, Object>() {{
                put("commentText", commentText);
            }});

            apiInstance.sendTransacEmail(sendSmtpEmail);
        } catch (ApiException e) {
            System.err.println("Exception when calling TransactionalEmailsApi#sendTransacEmail");
            e.printStackTrace();
        }
    }*/


}







