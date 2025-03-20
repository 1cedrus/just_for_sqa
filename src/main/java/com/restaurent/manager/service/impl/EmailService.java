package com.restaurent.manager.service.impl;

import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.service.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class EmailService implements IEmailService {
    @Autowired
    private JavaMailSender javaMailSender;


    @Override
    public void sendEmail(String email, String body, String subject) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("VietKitchen");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(body, true); // The 'true' flag indicates that the content is HTML
            javaMailSender.send(mimeMessage);
        }catch (MailException e){
           throw new AppException(ErrorCode.EMAIL_NOT_EXIST);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public String generateCode(int length) {
        Random random = new Random();
        int randomNumber = random.nextInt(999999);
        String output = Integer.toString(randomNumber);
        while (output.length() < length){
            output = "0" + output;
        }
        return output;
    }

}
