package com.soundhub.api.security;

import com.soundhub.api.exceptions.RefreshTokenExpiredException;
import com.soundhub.api.exceptions.RefreshTokenNotFoundException;
import com.soundhub.api.models.User;
import com.soundhub.api.repositories.RefreshTokenRepository;
import com.soundhub.api.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	private final UserService userService;
	private final RefreshTokenRepository refreshTokenRepository;

	@Value("${refreshToken.expirationInMs}")
	private int refreshTokenExpiration;

	public RefreshToken createRefreshToken(String email) {
		User user = userService.getUserByEmail(email);

		return refreshTokenRepository.findByUser(user).orElseGet(() -> {
			RefreshToken token = RefreshToken.builder()
					.refreshToken(UUID.randomUUID().toString())
					.expirationTime(Instant.now().plusMillis(refreshTokenExpiration))
					.user(user)
					.build();

			return refreshTokenRepository.save(token);
		});
	}

	public Optional<RefreshToken> getRefreshTokenIfExistsByUser(User user) {
		return refreshTokenRepository.findByUser(user);
	}

	public RefreshToken verifyRefreshToken(String refreshToken) {
		RefreshToken token = refreshTokenRepository.findByRefreshToken(refreshToken)
				.orElseThrow(() -> new RefreshTokenNotFoundException(refreshToken));

		if (token.getExpirationTime().compareTo(Instant.now()) < 0) {
			refreshTokenRepository.delete(token);
			throw new RefreshTokenExpiredException(token);
		}

		return token;
	}

	public void deleteRefreshToken(String refreshToken) {
		RefreshToken token = refreshTokenRepository.findByRefreshToken(refreshToken)
				.orElseThrow(() -> new RefreshTokenNotFoundException(refreshToken));

		refreshTokenRepository.delete(token);
	}
}
