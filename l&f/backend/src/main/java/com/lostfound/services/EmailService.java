package com.lostfound.services;

import com.lostfound.entities.Claim;
import com.lostfound.entities.Item;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private EmailTemplateService templateService;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@lostfoundportal.com}")
    private String fromEmail;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Async("emailTaskExecutor")
    public void sendRegistrationSuccessEmail(String toEmail, String fullName, String username) {
        if (!shouldSend(toEmail)) return;
        String subject = "Welcome to Lost & Found Portal - Registration Confirmed";
        String html = templateService.buildRegistrationWelcomeHtml(fullName, username);
        sendHtmlEmail(toEmail, subject, html);
    }

    @Async("emailTaskExecutor")
    public void sendItemReportedEmail(String toEmail, String fullName, Item item) {
        if (!shouldSend(toEmail) || item == null) return;
        String subject = "Listing Report Confirmed: " + item.getTitle();
        String html = templateService.buildItemReportedHtml(fullName, item);
        sendHtmlEmail(toEmail, subject, html);
    }

    @Async("emailTaskExecutor")
    public void sendNewClaimReceivedEmail(String toEmail, String ownerName, Item item, Claim claim) {
        if (!shouldSend(toEmail) || item == null || claim == null) return;
        String subject = "New Ownership Claim Submitted: " + item.getTitle();
        String html = templateService.buildNewClaimReceivedHtml(ownerName, item, claim);
        sendHtmlEmail(toEmail, subject, html);
    }

    @Async("emailTaskExecutor")
    public void sendClaimApprovedEmail(String toEmail, String claimantName, Item item, String remarks) {
        if (!shouldSend(toEmail) || item == null) return;
        String subject = "Claim Approved: " + item.getTitle();
        String html = templateService.buildClaimApprovedHtml(claimantName, item, remarks);
        sendHtmlEmail(toEmail, subject, html);
    }

    @Async("emailTaskExecutor")
    public void sendClaimRejectedEmail(String toEmail, String claimantName, Item item, String remarks) {
        if (!shouldSend(toEmail) || item == null) return;
        String subject = "Claim Status Update: " + item.getTitle();
        String html = templateService.buildClaimRejectedHtml(claimantName, item, remarks);
        sendHtmlEmail(toEmail, subject, html);
    }

    @Async("emailTaskExecutor")
    public void sendItemRecoveredEmail(String toEmail, String recipientName, Item item) {
        if (!shouldSend(toEmail) || item == null) return;
        String subject = "Item Recovered: " + item.getTitle();
        String html = templateService.buildItemRecoveredHtml(recipientName, item);
        sendHtmlEmail(toEmail, subject, html);
    }

    @Async("emailTaskExecutor")
    public void sendPotentialMatchAlertEmail(String toEmail, String recipientName, Item originalItem, List<Item> matches) {
        if (!shouldSend(toEmail) || originalItem == null || matches == null || matches.isEmpty()) return;
        String subject = "Potential Match Found for " + originalItem.getTitle();
        String html = templateService.buildPotentialMatchHtml(recipientName, originalItem, matches);
        sendHtmlEmail(toEmail, subject, html);
    }

    private boolean shouldSend(String toEmail) {
        if (!mailEnabled) {
            logger.debug("Email notifications are disabled by configuration");
            return false;
        }
        if (toEmail == null || toEmail.trim().isEmpty() || !toEmail.contains("@")) {
            logger.warn("Skipping email dispatch: Invalid recipient address '{}'", toEmail);
            return false;
        }
        return true;
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            // If mail sender or SMTP username is not configured, simulate delivery for local development
            if (mailSender == null || smtpUsername == null || smtpUsername.trim().isEmpty()) {
                logger.info("[DEV EMAIL SIMULATION] To: {} | Subject: '{}' | Notification successfully prepared without active SMTP credentials.",
                        to, subject);
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to.trim());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Successfully sent email notification to {} with subject '{}'", to, subject);
        } catch (Exception ex) {
            logger.error("Failed to send email to {} with subject '{}': {}", to, subject, ex.getMessage());
            // Do not rethrow - email failures must not break business operations
        }
    }
}
