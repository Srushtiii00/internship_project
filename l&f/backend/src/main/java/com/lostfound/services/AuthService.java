package com.lostfound.services;

import com.lostfound.config.JwtTokenProvider;
import com.lostfound.dto.request.LoginRequest;
import com.lostfound.dto.request.RegisterRequest;
import com.lostfound.dto.response.ApiResponse;
import com.lostfound.dto.response.AuthResponse;
import com.lostfound.entities.User;
import com.lostfound.entities.enums.Role;
import com.lostfound.exceptions.BadRequestException;
import com.lostfound.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private EmailService emailService;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.email:admin@lostfound.com}")
    private String adminEmail;

    @Value("${app.admin.fullname:System Administrator}")
    private String adminFullname;

    @Value("${app.admin.phone:9876543210}")
    private String adminPhone;

    @Value("${app.admin.reset-password-on-startup:false}")
    private boolean resetAdminPasswordOnStartup;

    @PostConstruct
    public void initDefaultAdmin() {
        if (adminPassword == null || adminPassword.trim().isEmpty()) {
            return;
        }

        Optional<User> existingAdmin = userRepository.findByUsername(adminUsername);
        if (existingAdmin.isPresent()) {
            User admin = existingAdmin.get();
            if (resetAdminPasswordOnStartup) {
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setActive(true);
                admin.setRole(Role.ROLE_ADMIN);
                userRepository.save(admin);
                logger.info("Admin account '{}' password successfully synchronized with configured APP_ADMIN_PASSWORD (active=true, role=ROLE_ADMIN).", adminUsername);
            }
        } else {
            User admin = new User(
                    adminUsername,
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    adminFullname,
                    adminPhone,
                    Role.ROLE_ADMIN
            );
            admin.setActive(true);
            userRepository.save(admin);
            logger.info("Default administrator account created with username: {}", adminUsername);
        }
    }

    @Transactional
    public ApiResponse<String> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered!");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                request.getPhoneNumber(),
                Role.ROLE_USER
        );

        User savedUser = userRepository.save(user);
        emailService.sendRegistrationSuccessEmail(savedUser.getEmail(), savedUser.getFullName(), savedUser.getUsername());
        return ApiResponse.success("User registered successfully!");
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new BadRequestException("User not found"));

        return new AuthResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );
    }
}
