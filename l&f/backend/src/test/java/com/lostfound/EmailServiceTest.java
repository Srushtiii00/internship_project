package com.lostfound;

import com.lostfound.entities.Claim;
import com.lostfound.entities.Item;
import com.lostfound.entities.User;
import com.lostfound.entities.enums.ItemStatus;
import com.lostfound.entities.enums.ItemType;
import com.lostfound.entities.enums.Role;
import com.lostfound.services.EmailService;
import com.lostfound.services.EmailTemplateService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailTemplateService templateService;

    @InjectMocks
    private EmailService emailService;

    private EmailTemplateService realTemplateService;
    private User testUser;
    private Item testItem;
    private Claim testClaim;

    @BeforeEach
    void setUp() {
        realTemplateService = new EmailTemplateService();
        ReflectionTestUtils.setField(realTemplateService, "portalName", "Lost & Found Portal");
        ReflectionTestUtils.setField(realTemplateService, "frontendUrl", "http://localhost:5173");

        testUser = new User("alice", "alice@example.com", "secretHash", "Alice Smith", "1234567890", Role.ROLE_USER);
        testUser.setId(1L);

        testItem = new Item(testUser, "Blue Hydro Flask", "32oz navy blue bottle", "Bottles", ItemType.FOUND, ItemStatus.FOUND, "Gym Area", LocalDate.now(), null);
        testItem.setId(42L);

        User claimant = new User("bob", "bob@example.com", "secretHash2", "Bob Jones", "9876543210", Role.ROLE_USER);
        claimant.setId(2L);
        testClaim = new Claim(testItem, claimant, "Dent on bottom right rim", "bob@example.com");
        testClaim.setId(99L);
    }

    @Test
    @DisplayName("Email Templates: Registration email includes user details and no password")
    void testRegistrationTemplate() {
        String html = realTemplateService.buildRegistrationWelcomeHtml("Alice Smith", "alice");

        assertNotNull(html);
        assertTrue(html.contains("Alice Smith"));
        assertTrue(html.contains("alice"));
        assertTrue(html.contains("Lost &amp; Found Portal") || html.contains("Lost & Found Portal"));
        assertTrue(html.contains("/login"));
        assertFalse(html.contains("secretHash"), "Email must never leak password hash");
    }

    @Test
    @DisplayName("Email Templates: Item reported email includes title, category, and location")
    void testItemReportedTemplate() {
        String html = realTemplateService.buildItemReportedHtml("Alice Smith", testItem);

        assertNotNull(html);
        assertTrue(html.contains("Blue Hydro Flask"));
        assertTrue(html.contains("Bottles"));
        assertTrue(html.contains("Gym Area"));
        assertTrue(html.contains("/items/42"));
    }

    @Test
    @DisplayName("Email Templates: New claim email includes claim proof and claimant name")
    void testNewClaimTemplate() {
        String html = realTemplateService.buildNewClaimReceivedHtml("Alice Smith", testItem, testClaim);

        assertNotNull(html);
        assertTrue(html.contains("Bob Jones"));
        assertTrue(html.contains("Dent on bottom right rim"));
        assertTrue(html.contains("bob@example.com"));
        assertTrue(html.contains("/items/42"));
    }

    @Test
    @DisplayName("Email Templates: Claim approval email includes handover remarks")
    void testClaimApprovedTemplate() {
        String html = realTemplateService.buildClaimApprovedHtml("Bob Jones", testItem, "Available at Student Center desk");

        assertNotNull(html);
        assertTrue(html.contains("APPROVED"));
        assertTrue(html.contains("Available at Student Center desk"));
        assertTrue(html.contains("Blue Hydro Flask"));
        assertTrue(html.contains("/dashboard"));
    }

    @Test
    @DisplayName("Email Templates: Claim rejection email includes decision reason")
    void testClaimRejectedTemplate() {
        String html = realTemplateService.buildClaimRejectedHtml("Bob Jones", testItem, "Color does not match reported bottle");

        assertNotNull(html);
        assertTrue(html.contains("Color does not match reported bottle"));
        assertTrue(html.contains("Blue Hydro Flask"));
        assertTrue(html.contains("/browse"));
    }

    @Test
    @DisplayName("Email Templates: Potential match alert includes matching listings")
    void testPotentialMatchTemplate() {
        String html = realTemplateService.buildPotentialMatchHtml("Alice Smith", testItem, Collections.singletonList(testItem));

        assertNotNull(html);
        assertTrue(html.contains("Potential Item Match Detected"));
        assertTrue(html.contains("Blue Hydro Flask"));
        assertTrue(html.contains("/items/42"));
    }

    @Test
    @DisplayName("Failure Tolerance: Mail delivery exception does not throw and does not fail caller")
    void testEmailFailure_NonBlockingTolerance() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@lostfoundportal.com");
        ReflectionTestUtils.setField(emailService, "smtpUsername", "smtp_user@gmail.com");

        when(templateService.buildRegistrationWelcomeHtml("Alice Smith", "alice")).thenReturn("<html>Welcome</html>");

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP server connection timeout")).when(mailSender).send(any(MimeMessage.class));

        // Must execute cleanly without throwing MailSendException
        assertDoesNotThrow(() ->
                emailService.sendRegistrationSuccessEmail("alice@example.com", "Alice Smith", "alice")
        );

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Dev Simulation: When SMTP username is unconfigured, simulate email without calling mailSender.send")
    void testDevSimulation_NoSmtpCredentials() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        ReflectionTestUtils.setField(emailService, "smtpUsername", ""); // empty credentials in local dev

        when(templateService.buildRegistrationWelcomeHtml("Alice Smith", "alice")).thenReturn("<html>Welcome</html>");

        assertDoesNotThrow(() ->
                emailService.sendRegistrationSuccessEmail("alice@example.com", "Alice Smith", "alice")
        );

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
