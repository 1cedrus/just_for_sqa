package com.restaurent.manager.service.impl;

import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.service.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class EmailService implements IEmailService {

    // Injecting the JavaMailSender to handle email sending
    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * Sends an email to the specified recipient.
     *
     * @param email   The recipient's email address.
     * @param body    The content of the email (HTML format).
     * @param subject The subject of the email.
     * @throws AppException if there is an issue with sending the email.
     */
    @Override
    public void sendEmail(String email, String body, String subject) {
        // Create a new MIME message for the email
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            // Helper to configure the email details
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("VietKitchen"); // Set the sender's name
            helper.setTo(email); // Set the recipient's email
            helper.setSubject(subject); // Set the email subject
            helper.setText(body, true); // Set the email body (HTML content)

            // Send the email
            javaMailSender.send(mimeMessage);
        } catch (MailException e) {
            // Handle email sending failure (e.g., invalid email address)
            throw new AppException(ErrorCode.EMAIL_NOT_EXIST);
        } catch (MessagingException e) {
            // Handle other messaging-related exceptions
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Generates a random numeric code of the specified length.
     *
     * @param length The desired length of the generated code.
     * @return A string representing the generated numeric code.
     */
    @Override
    public String generateCode(int length) {
        Random random = new Random();
        // Generate a random number up to 6 digits
        int randomNumber = random.nextInt(999999);
        String output = Integer.toString(randomNumber);

        // Pad the number with leading zeros if it's shorter than the desired length
        while (output.length() < length) {
            output = "0" + output;
        }
        return output;
    }
}