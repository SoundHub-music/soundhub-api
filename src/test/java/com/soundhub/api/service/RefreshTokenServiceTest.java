package com.soundhub.api.service;

import com.soundhub.api.exception.RefreshTokenExpiredException;
import com.soundhub.api.exception.RefreshTokenNotFoundException;
import com.soundhub.api.model.User;
import com.soundhub.api.repository.RefreshTokenRepository;
import com.soundhub.api.security.RefreshToken;
import com.soundhub.api.security.RefreshTokenService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {
    private static User user;
    private final int refreshTokenExpiration = 100000;

    @Mock
    private UserService userService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeAll
    static void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .build();
    }

    @Test
    void testCreateRefreshToken_WhenTokenDoesNotExist() {
        String email = user.getEmail();
        RefreshToken newToken = RefreshToken.builder()
                .refreshToken(UUID.randomUUID().toString())
                .expirationTime(Instant.now().plusMillis(refreshTokenExpiration))
                .user(user)
                .build();

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newToken);

        RefreshToken result = refreshTokenService.createRefreshToken(email);

        assertNotNull(result);
        assertEquals(newToken.getRefreshToken(), result.getRefreshToken());
        assertEquals(user, result.getUser());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testCreateRefreshToken_WhenTokenAlreadyExists() {
        String email = user.getEmail();
        RefreshToken existingToken = RefreshToken.builder()
                .refreshToken(UUID.randomUUID().toString())
                .expirationTime(Instant.now().plusMillis(refreshTokenExpiration))
                .user(user)
                .build();

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(existingToken));

        RefreshToken result = refreshTokenService.createRefreshToken(email);

        assertEquals(existingToken, result);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void testGetRefreshTokenIfExistsByUser_WhenTokenExists() {
        RefreshToken expectedToken = RefreshToken.builder()
                .user(user)
                .build();

        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(expectedToken));

        Optional<RefreshToken> result = refreshTokenService.getRefreshTokenIfExistsByUser(user);

        assertTrue(result.isPresent());
        assertEquals(expectedToken, result.get());
    }

    @Test
    void testGetRefreshTokenIfExistsByUser_WhenTokenDoesNotExist() {
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        Optional<RefreshToken> result = refreshTokenService.getRefreshTokenIfExistsByUser(user);

        assertTrue(result.isEmpty());
    }

    @Test
    void testVerifyRefreshToken_WhenTokenIsValid() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken token = RefreshToken.builder()
                .refreshToken(tokenValue)
                .expirationTime(Instant.now().plusMillis(refreshTokenExpiration))
                .build();

        when(refreshTokenRepository.findByRefreshToken(tokenValue)).thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.verifyRefreshToken(tokenValue);

        assertEquals(token, result);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void testVerifyRefreshToken_WhenTokenDoesNotExist() {
        String tokenValue = "non-existent-token";
        when(refreshTokenRepository.findByRefreshToken(tokenValue)).thenReturn(Optional.empty());

        RefreshTokenNotFoundException exception = assertThrows(
                RefreshTokenNotFoundException.class,
                () -> refreshTokenService.verifyRefreshToken(tokenValue)
        );

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(tokenValue));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testVerifyRefreshToken_WhenTokenIsExpired() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken expiredToken = RefreshToken.builder()
                .refreshToken(tokenValue)
                .expirationTime(Instant.now().minusMillis(1))
                .build();

        when(refreshTokenRepository.findByRefreshToken(tokenValue)).thenReturn(Optional.of(expiredToken));

        assertThrows(RefreshTokenExpiredException.class,
                () -> refreshTokenService.verifyRefreshToken(tokenValue));

        verify(refreshTokenRepository, times(1)).delete(expiredToken);
    }

    @Test
    void testDeleteRefreshToken_WhenTokenExists() {
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken token = RefreshToken.builder()
                .refreshToken(tokenValue)
                .build();

        when(refreshTokenRepository.findByRefreshToken(tokenValue)).thenReturn(Optional.of(token));

        assertDoesNotThrow(() -> refreshTokenService.deleteRefreshToken(tokenValue));
        verify(refreshTokenRepository, times(1)).delete(token);
    }

    @Test
    void testDeleteRefreshToken_WhenTokenDoesNotExist() {
        String tokenValue = "non-existent-token";
        when(refreshTokenRepository.findByRefreshToken(tokenValue)).thenReturn(Optional.empty());

        RefreshTokenNotFoundException exception = assertThrows(
                RefreshTokenNotFoundException.class,
                () -> refreshTokenService.deleteRefreshToken(tokenValue)
        );

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(tokenValue));
        assertTrue(exception.getMessage().contains("not found"));
        verify(refreshTokenRepository, never()).delete(any());
    }
}