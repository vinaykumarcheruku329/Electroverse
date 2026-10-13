package com.electroverse.security;

import com.electroverse.entity.User;
import com.electroverse.exception.ResourceNotFoundException;
import com.electroverse.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        return null;
    }

    public User getCurrentUser() {
        UserPrincipal principal = getCurrentPrincipal();
        if (principal == null) {
            throw new ResourceNotFoundException("No authenticated user");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user no longer exists"));
    }

    public Long getCurrentUserId() {
        UserPrincipal principal = getCurrentPrincipal();
        return principal == null ? null : principal.getId();
    }
}
