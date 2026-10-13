package com.electroverse.service;

import com.electroverse.dto.request.AuthRequests;
import com.electroverse.dto.response.AuthResponses;
import com.electroverse.entity.User;
import com.electroverse.entity.enums.Role;
import com.electroverse.exception.ConflictException;
import com.electroverse.mapper.DtoMapper;
import com.electroverse.repository.UserRepository;
import com.electroverse.security.JwtService;
import com.electroverse.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponses.Auth register(AuthRequests.Register request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone());
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        User saved = userRepository.save(user);
        UserPrincipal principal = new UserPrincipal(saved);
        return buildAuth(saved, principal);
    }

    @Transactional(readOnly = true)
    public AuthResponses.Auth login(AuthRequests.Login request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ConflictException("Account not found"));
        return buildAuth(user, principal);
    }

    private AuthResponses.Auth buildAuth(User user, UserPrincipal principal) {
        String token = jwtService.generateToken(principal);
        return new AuthResponses.Auth(token, "Bearer", jwtService.getExpirationMs(), DtoMapper.toUser(user));
    }
}
