package com.service.event.api;

import com.service.event.domain.Utilisateur;
import com.service.event.payload.response.BadRequestException;
import com.service.event.payload.response.ForgotPasswordRequest;
import com.service.event.payload.response.ResetPasswordRequest;
import com.service.event.repository.UserRepository;
import com.service.event.service.SendinblueTransactionalEmailsApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.webjars.NotFoundException;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/forgotpwd")
public class ForgotPasswordController {
    private final UserRepository userRepository;
    private final SendinblueTransactionalEmailsApi emailApi;

    @Autowired
    public ForgotPasswordController(UserRepository userRepository, SendinblueTransactionalEmailsApi emailApi) {
        this.userRepository = userRepository;
        this.emailApi = emailApi;
    }
    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String email = request.getEmail();

        if (StringUtils.hasText(email)) {
            Utilisateur user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("User not found."));

            // Generate a unique reset token
            String resetToken = UUID.randomUUID().toString();

            // Save the reset token in the user entity
            user.setResetToken(resetToken);
            userRepository.save(user);

            // Send the password reset email
           // sendPasswordResetEmail(user.getEmail(), resetToken);
            sendMail("new comment  ","ryma", "ryma.bensalah@esprit.tn",
                    "Visit this url : ");
        } else {
            throw new BadRequestException("Email is required.");
        }
    }

    private void sendPasswordResetEmail(String email, String resetToken) {

        // Configure the Sendinblue API client
        String apiKey = "xsmtpsib-b0991b9b9a62b408074b0a9d0b1af3a923a4e32294b52553b044d0d4bd30dee2-CjZTb58SwntzH9Dx"; // Replace with your Sendinblue API key
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setApiKey(apiKey);

        // Create an instance of the Sendinblue TransactionalEmailsApi
        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

        // Construct the email content
        SendSmtpEmail sender = new SendSmtpEmail();
        sender.setSender(new SendSmtpEmailSender().email("ryma.bensalah@esprit.tn").name("Ryma"));
        sender.setTo(new ArrayList<SendSmtpEmailTo>(Arrays.asList(new SendSmtpEmailTo().email(email))));
        sender.setSubject("Password Reset");
        sender.setTextContent("Please click on the link below to reset your password: \n\n" +
                "Reset Link: https://example.com/reset-password?token=" + resetToken);

        // Send the email using the Sendinblue API client
        try {
            CreateSmtpEmail response = apiInstance.sendTransacEmail(sender);
            System.out.println("Password reset email sent successfully. Message ID: " + response.getMessageId());
        } catch (ApiException e) {
            System.err.println("Error sending password reset email: " + e.getResponseBody());
        }
    }
    public void sendMail(String subject, String recepientName, String recepientEmail, String content) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        // Configure API key authorization: api-key
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey("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");

        try {
            TransactionalEmailsApi api = new TransactionalEmailsApi();
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail("g.meda69@gmail.com");
            sender.setName("Ryma");
            List<SendSmtpEmailTo> toList = new ArrayList<>();
            SendSmtpEmailTo to = new SendSmtpEmailTo();
            to.setEmail(recepientEmail); // to make dynamic
            to.setName(recepientName); // to make dynamic
            toList.add(to);

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(toList);
            sendSmtpEmail.setTemplateId(1L);
            sendSmtpEmail.setHtmlContent("<html><body>" + content + "</body></html>");
            sendSmtpEmail.setSubject(subject);

            CreateSmtpEmail response = api.sendTransacEmail(sendSmtpEmail);
            System.out.println(response.toString());
        } catch (Exception e) {
            System.out.println("Exception occurred:- " + e.getMessage());
            e.printStackTrace();
        }
    }

}
