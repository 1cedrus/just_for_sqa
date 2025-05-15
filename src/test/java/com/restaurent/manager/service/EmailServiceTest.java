package com.restaurent.manager.service;

import com.restaurent.manager.exception.AppException;
import com.restaurent.manager.exception.ErrorCode;
import com.restaurent.manager.service.impl.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    // Mock dependency JavaMailSender
    @Mock
    private JavaMailSender javaMailSender;

    // Inject mock vào EmailService
    @InjectMocks
    private EmailService emailService;

    // Biến mẫu để tái sử dụng
    private MimeMessage mimeMessage;

    @BeforeEach
    public void setUp() {
        // Tạo MimeMessage mẫu
        mimeMessage = mock(MimeMessage.class);
    }

    /**
     * Test sendEmail khi gửi email thành công.
     * Kiểm tra nhánh không có exception.
     */
    // TestcaseID: ES-1
    @Test
    public void testSendEmail_Success() {
        // Mock tạo MimeMessage
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Mock MimeMessageHelper không ném exception
        doNothing().when(javaMailSender).send(mimeMessage);

        // Gọi phương thức cần test
        emailService.sendEmail("test@example.com", "<h1>Hello</h1>", "Test Subject");

        // Xác nhận gửi email được gọi
        verify(javaMailSender).send(mimeMessage);
    }

    /**
     * Test sendEmail khi gặp MailException.
     * Kiểm tra nhánh catch MailException.
     */
    // TestcaseID: ES-2
    @Test
    public void testSendEmail_MailException() throws MessagingException, IOException {
        // Mock tạo MimeMessage
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Mock gửi email ném MailException
        doThrow(new MailException("Mail error") {}).when(javaMailSender).send(mimeMessage);

        // Gọi phương thức và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            emailService.sendEmail("test@example.com", "<h1>Hello</h1>", "Test Subject");
        });

        // Xác nhận lỗi là EMAIL_NOT_EXIST
        assertEquals(ErrorCode.EMAIL_NOT_EXIST, exception.getErrorCode(), "Lỗi phải là EMAIL_NOT_EXIST");
    }

    /**
     * Test sendEmail khi gặp MessagingException.
     * Kiểm tra nhánh catch MessagingException.
     */
    // TestcaseID: ES-3
    @Test
    public void testSendEmail_MessagingException() throws MessagingException {
        // Mock tạo MimeMessage
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Mock MimeMessageHelper ném MessagingException
        doThrow(new MessagingException("Messaging error")).when(mimeMessage).setContent(any());

        // Gọi phương thức và kiểm tra exception
        AppException exception = assertThrows(AppException.class, () -> {
            emailService.sendEmail("test@example.com", "<h1>Hello</h1>", "Test Subject");
        });

        // Xác nhận lỗi là UNCATEGORIZED_EXCEPTION
        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, exception.getErrorCode(), "Lỗi phải là UNCATEGORIZED_EXCEPTION");
    }

    // TestcaseID: ES-4
    @Test
    public void testGenerateCode() {
        // Để đảm bảo random.nextInt(999999) trả về số có độ dài 6 (ví dụ: 123456),
        String result = emailService.generateCode(6);

        // Xác nhận kết quả
        assertEquals(6, result.length(), "Độ dài mã phải là 6");
        assertTrue(result.matches("\\d+"), "Mã phải là số");
    }
}
