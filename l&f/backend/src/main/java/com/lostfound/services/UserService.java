package com.lostfound.services;

import com.lostfound.dto.response.UserSummaryResponse;
import com.lostfound.entities.User;
import com.lostfound.exceptions.ResourceNotFoundException;
import com.lostfound.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserSummaryResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return UserSummaryResponse.fromEntity(user);
    }

    @Transactional
    public UserSummaryResponse updateProfile(Long userId, String fullName, String phoneNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }
        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber.trim());
        }

        User updated = userRepository.save(user);
        return UserSummaryResponse.fromEntity(updated);
    }
}
