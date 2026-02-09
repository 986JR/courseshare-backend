package backend.courseshare.service;

import com.sendgrid.SendGrid;
//import jakarta.validation.constraints.Email;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.sendgrid.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final SendGrid sendGrid;

    public EmailService(JavaMailSender mailSender, SendGrid sendGrid) {

        this.mailSender = mailSender;
        this.sendGrid=sendGrid;
    }

    @Async
    public void sendEmail(String to, String subject, String body) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(body);
//        message.setFrom("app.smerck.j9@gmail.com");
//        mailSender.send(message);
        Email from = new Email("app.smerck.j9@gmail.com");
        Email toEmail = new Email(to);
        Content content = new Content("text/plain",body);
        Mail mail = new Mail(from, subject,toEmail,content);

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            System.out.println("Email Sent! Status code: "+response.getStatusCode());
        } catch( IOException ex) {
            System.err.println("Email Sending Failled: " + ex.getMessage());
        }
    }
}
