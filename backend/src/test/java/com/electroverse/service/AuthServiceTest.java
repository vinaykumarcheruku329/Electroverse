package com.electroverse.service;

import com.electroverse.dto.request.AuthRequests;
import com.electroverse.dto.response.AuthResponses;
import com.electroverse.entity.User;
import com.electroverse.entity.enums.Role;
import com.electroverse.exception.ConflictException;
import com.electroverse.repository.UserRepository;
import com.electroverse.security.JwtService;
import com.electroverse.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    void registerHashesPasswordAndReturnsToken() {
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("token-123");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        AuthResponses.Auth auth = authService.register(
                new AuthRequests.Register("User@Test.com", "Password123", "Test User", "123"));

        assertThat(auth.token()).isEqualTo("token-123");
        assertThat(auth.user().email()).isEqualTo("user@test.com");
        assertThat(auth.user().role()).isEqualTo("CUSTOMER");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new AuthRequests.Register("dup@test.com", "Password123", "Dup", null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        User user = new User();
        user.setId(2L);
        user.setEmail("login@test.com");
        user.setFullName("Login User");
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        user.setPassword("$2a$hashed");
        UserPrincipal principal = new UserPrincipal(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("jwt-login");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        AuthResponses.Auth auth = authService.login(new AuthRequests.Login("login@test.com", "Password123"));

        assertThat(auth.token()).isEqualTo("jwt-login");
        assertThat(auth.user().email()).isEqualTo("login@test.com");
    }
}
