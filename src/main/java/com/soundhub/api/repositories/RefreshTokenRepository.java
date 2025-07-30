package com.soundhub.api.repositories;

import com.soundhub.api.models.User;
import com.soundhub.api.security.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
	Optional<RefreshToken> findByRefreshToken(String refreshToken);

	Optional<RefreshToken> findByUser(User user);
}
