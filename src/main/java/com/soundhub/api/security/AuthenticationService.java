package com.soundhub.api.security;

import com.soundhub.api.Constants;
import com.soundhub.api.dto.SignInDto;
import com.soundhub.api.dto.UserDto;
import com.soundhub.api.dto.request.RefreshTokenRequest;
import com.soundhub.api.dto.response.AuthResponse;
import com.soundhub.api.dto.response.LogoutResponse;
import com.soundhub.api.exceptions.InvalidEmailOrPasswordException;
import com.soundhub.api.exceptions.UserAlreadyExistsException;
import com.soundhub.api.models.User;
import com.soundhub.api.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	private final UserService userService;
	private final JwtService jwtService;
	private final BlacklistingService blacklistingService;
	private final RefreshTokenService refreshTokenService;
	private final AuthenticationManager authenticationManager;

	public AuthResponse signUp(UserDto userDto, MultipartFile file) throws IOException {
		if (userService.checkEmailAvailability(userDto.getEmail())) {
			throw new UserAlreadyExistsException(Constants.USER_EMAIL_EXISTS_MSG);
		}

		User user = userService.addUser(userDto, file);
		String email = user.getEmail();

		String jwt = jwtService.generateToken(user);
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);

		return AuthResponse.builder()
				.accessToken(jwt)
				.refreshToken(refreshToken.getRefreshToken())
				.build();
	}

	public AuthResponse signIn(SignInDto signInDto) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(signInDto.getEmail(), signInDto.getPassword())
			);
		} catch (AuthenticationException e) {
			throw new InvalidEmailOrPasswordException(Constants.INVALID_EMAIL_PASSWORD);
		}

		var user = userService.getUserByEmail(signInDto.getEmail());

		var jwt = jwtService.generateToken(user);
		var refreshToken = refreshTokenService.createRefreshToken(signInDto.getEmail());

		return AuthResponse.builder()
				.accessToken(jwt)
				.refreshToken(refreshToken.getRefreshToken())
				.build();
	}

	public AuthResponse refreshToken(RefreshTokenRequest request) {
		RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
		User user = refreshToken.getUser();
		String accessToken = jwtService.generateToken(user);

		return AuthResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken.getRefreshToken())
				.build();
	}

	public LogoutResponse logout(String authHeader) {
		String jwt = authHeader.substring(Constants.BEARER_PREFIX.length());
		String username = jwtService.extractUsername(jwt);
		User currentUser = userService.getUserByEmail(username);

		refreshTokenService.getRefreshTokenIfExistsByUser(currentUser).ifPresent(value -> {
			String tokenValue = value.getRefreshToken();

			blacklistingService.blackListJwt(jwt);
			refreshTokenService.deleteRefreshToken(tokenValue);
		});

		return new LogoutResponse(Constants.SUCCESSFUL_LOGOUT);
	}
}
