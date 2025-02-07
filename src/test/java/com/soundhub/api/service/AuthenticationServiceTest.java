package com.soundhub.api.service;

import com.soundhub.api.Constants;
import com.soundhub.api.dto.SignInDto;
import com.soundhub.api.dto.UserDto;
import com.soundhub.api.dto.request.RefreshTokenRequest;
import com.soundhub.api.dto.response.AuthResponse;
import com.soundhub.api.dto.response.LogoutResponse;
import com.soundhub.api.exception.InvalidEmailOrPasswordException;
import com.soundhub.api.exception.UserAlreadyExistsException;
import com.soundhub.api.model.User;
import com.soundhub.api.security.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    private final String testEmail = "test@example.com";
    private final String testPassword = "password";
    private final String testToken = "test-token";
    private final String refreshToken = "refresh-token";
    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private BlacklistingService blacklistingService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private MultipartFile file;
    @InjectMocks
    private AuthenticationService authenticationService;
    private UserDto userDto;
    private SignInDto signInDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .email(testEmail)
                .password(testPassword)
                .build();

        signInDto = SignInDto.builder()
                .email(testEmail)
                .password(testPassword)
                .build();

        user = User.builder()
                .id(UUID.randomUUID())
                .email(testEmail)
                .password(testPassword)
                .build();
    }

    @Test
    void signUp_ShouldThrowWhenUserExists() throws IOException {
        when(userService.checkEmailAvailability(testEmail)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authenticationService.signUp(userDto, file));

        verify(userService, never()).addUser(any(), any());
    }

    @Test
    void signUp_ShouldReturnAuthResponseWhenSuccess() throws IOException {
        when(userService.checkEmailAvailability(testEmail)).thenReturn(false);
        when(userService.addUser(any(), any())).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(testToken);
        when(refreshTokenService.createRefreshToken(testEmail))
                .thenReturn(new RefreshToken(1, refreshToken, null, user));

        AuthResponse response = authenticationService.signUp(userDto, file);

        assertNotNull(response);
        assertEquals(testToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(userService).addUser(any(), any());
        verify(jwtService).generateToken(user);
    }

    @Test
    void signIn_ShouldThrowWhenInvalidCredentials() {
        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager)
                .authenticate(any());

        assertThrows(InvalidEmailOrPasswordException.class,
                () -> authenticationService.signIn(signInDto));
    }

    @Test
    void signIn_ShouldReturnAuthResponseWhenSuccess() {
        when(userService.getUserByEmail(testEmail)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(testToken);
        when(refreshTokenService.createRefreshToken(testEmail))
                .thenReturn(new RefreshToken(1, refreshToken, null, user));

        AuthResponse response = authenticationService.signIn(signInDto);

        assertNotNull(response);
        assertEquals(testToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(authenticationManager).authenticate(any());
        verify(userService).getUserByEmail(testEmail);
    }

    @Test
    void refreshToken_ShouldReturnNewAccessToken() {
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        RefreshToken refreshTokenObj = new RefreshToken(1, refreshToken, null, user);

        when(refreshTokenService.verifyRefreshToken(refreshToken)).thenReturn(refreshTokenObj);
        when(jwtService.generateToken(user)).thenReturn(testToken);

        AuthResponse response = authenticationService.refreshToken(request);

        assertEquals(testToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(refreshTokenService).verifyRefreshToken(refreshToken);
    }

    @Test
    void logout_ShouldHandleUserWithoutRefreshToken() {
        String authHeader = "Bearer " + testToken;
        when(jwtService.extractUsername(testToken)).thenReturn(testEmail);
        when(userService.getUserByEmail(testEmail)).thenReturn(user);
        when(refreshTokenService.getRefreshTokenIfExistsByUser(user)).thenReturn(Optional.empty());

        LogoutResponse response = authenticationService.logout(authHeader);

        assertEquals(Constants.SUCCESSFUL_LOGOUT, response.getMessage());
        verify(blacklistingService, never()).blackListJwt(any());
        verify(refreshTokenService, never()).deleteRefreshToken(any());
    }

    @Test
    void logout_ShouldBlacklistTokenAndDeleteRefreshToken() {
        String authHeader = "Bearer " + testToken;
        RefreshToken refreshTokenObj = new RefreshToken(1, refreshToken, null, user);

        when(jwtService.extractUsername(testToken)).thenReturn(testEmail);
        when(userService.getUserByEmail(testEmail)).thenReturn(user);
        when(refreshTokenService.getRefreshTokenIfExistsByUser(user))
                .thenReturn(Optional.of(refreshTokenObj));

        LogoutResponse response = authenticationService.logout(authHeader);

        assertEquals(Constants.SUCCESSFUL_LOGOUT, response.getMessage());
        verify(blacklistingService).blackListJwt(testToken);
        verify(refreshTokenService).deleteRefreshToken(refreshToken);
    }
}