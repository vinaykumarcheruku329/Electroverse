package com.electroverse.service;

import com.electroverse.dto.request.AuthRequests;
import com.electroverse.dto.response.AuthResponses;
import com.electroverse.entity.User;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.repository.UserRepository;
import com.electroverse.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public UserService(UserRepository userRepository, SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public AuthResponses.User getCurrentUser() {
        return DtoMapper.toUser(securityUtils.getCurrentUser());
    }

    @Transactional
    public AuthResponses.User updateProfile(AuthRequests.UpdateProfile request) {
        User user = securityUtils.getCurrentUser();
        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone());
        return DtoMapper.toUser(userRepository.save(user));
    }
}
