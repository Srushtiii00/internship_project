package com.lostfound;

import com.lostfound.config.JwtTokenProvider;
import com.lostfound.config.UserDetailsImpl;
import com.lostfound.dto.request.RegisterRequest;
import com.lostfound.dto.response.ApiResponse;
import com.lostfound.entities.Item;
import com.lostfound.entities.User;
import com.lostfound.entities.enums.ItemStatus;
import com.lostfound.entities.enums.ItemType;
import com.lostfound.entities.enums.Role;
import com.lostfound.exceptions.BadRequestException;
import com.lostfound.repositories.ClaimRepository;
import com.lostfound.repositories.ItemRepository;
import com.lostfound.repositories.UserRepository;
import com.lostfound.services.AdminService;
import com.lostfound.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthSecurityTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.lostfound.services.EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @InjectMocks
    private AdminService adminService;

    @Test
    @DisplayName("Auth: Prevent duplicate username registration")
    void testRegister_DuplicateUsername() {
        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");
        request.setEmail("new@example.com");
        request.setPassword("Password123!");
        request.setFullName("Existing User");

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                authService.register(request)
        );
        assertTrue(ex.getMessage().contains("Username is already taken"));
    }

    @Test
    @DisplayName("Auth: Prevent duplicate email registration")
    void testRegister_DuplicateEmail() {
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("taken@example.com");
        request.setPassword("Password123!");
        request.setFullName("New User");

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                authService.register(request)
        );
        assertTrue(ex.getMessage().contains("Email is already registered"));
    }

    @Test
    @DisplayName("Account Status: Deactivated user (active = false) reflects isEnabled = false in UserDetailsImpl")
    void testDeactivatedUser_IsEnabledReturnsFalse() {
        User inactiveUser = new User("inactiveUser", "inactive@example.com", "pass", "Inactive User", "1234567890", Role.ROLE_USER);
        inactiveUser.setActive(false);

        UserDetailsImpl userDetails = UserDetailsImpl.build(inactiveUser);
        assertFalse(userDetails.isEnabled(), "UserDetailsImpl.isEnabled() must return false for inactive users");
    }

    @Test
    @DisplayName("Admin Protection: Admin cannot delete/deactivate their own account")
    void testAdminDelete_PreventSelfDeletion() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                adminService.deleteUser(1L, 1L)
        );
        assertTrue(ex.getMessage().contains("cannot delete or deactivate your own"));
    }

    @Test
    @DisplayName("Admin Protection: Prevent deleting accounts with ROLE_ADMIN")
    void testAdminDelete_PreventAdminAccountDeletion() {
        User targetAdmin = new User("admin2", "admin2@lostfound.com", "pass", "Admin Two", "1234567890", Role.ROLE_ADMIN);
        targetAdmin.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetAdmin));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                adminService.deleteUser(2L, 1L)
        );
        assertTrue(ex.getMessage().contains("Administrator accounts cannot be deleted"));
    }

    @Test
    @DisplayName("Safe Deactivation: If user has items, safely deactivate instead of hard delete")
    void testAdminDelete_SafelyDeactivatesUserWithItems() {
        User regularUser = new User("regularUser", "regular@example.com", "pass", "Regular User", "1234567890", Role.ROLE_USER);
        regularUser.setId(5L);
        regularUser.setActive(true);

        Item userItem = new Item(regularUser, "Item", "Desc", "Keys", ItemType.LOST, ItemStatus.LOST, "Desk", LocalDate.now(), null);

        when(userRepository.findById(5L)).thenReturn(Optional.of(regularUser));
        when(itemRepository.findByUserIdOrderByCreatedAtDesc(5L)).thenReturn(Collections.singletonList(userItem));

        String result = adminService.deleteUser(5L, 1L);

        assertFalse(regularUser.isActive(), "User active flag should be set to false");
        assertTrue(result.contains("safely deactivated"), "Result message should confirm safe deactivation");
        verify(userRepository).save(regularUser);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("Admin Password Reset: Synchronize existing admin password when reset flag is enabled")
    void testInitDefaultAdmin_ResetExistingAdminPassword() {
        User existingAdmin = new User("admin", "admin@lostfound.com", "oldHash", "System Administrator", "9876543210", Role.ROLE_ADMIN);
        existingAdmin.setActive(false); // test reactivation

        ReflectionTestUtils.setField(authService, "adminUsername", "admin");
        ReflectionTestUtils.setField(authService, "adminPassword", "Admin@Secure2026!");
        ReflectionTestUtils.setField(authService, "resetAdminPasswordOnStartup", true);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(existingAdmin));
        when(passwordEncoder.encode("Admin@Secure2026!")).thenReturn("newBcryptHash");

        authService.initDefaultAdmin();

        assertEquals("newBcryptHash", existingAdmin.getPassword());
        assertTrue(existingAdmin.isActive(), "Admin account should be set to active");
        assertEquals(Role.ROLE_ADMIN, existingAdmin.getRole());
        verify(userRepository).save(existingAdmin);
    }
}
