package com.soundhub.api.services.impl;

import com.soundhub.api.Constants;
import com.soundhub.api.dto.UserDto;
import com.soundhub.api.dto.response.UserExistenceResponse;
import com.soundhub.api.enums.Role;
import com.soundhub.api.exceptions.ApiException;
import com.soundhub.api.exceptions.ResourceNotFoundException;
import com.soundhub.api.models.User;
import com.soundhub.api.repositories.UserRepository;
import com.soundhub.api.services.FileService;
import com.soundhub.api.services.UserService;
import com.soundhub.api.services.strategies.media.MediaFileSourceStrategy;
import com.soundhub.api.services.strategies.media.MediaFileSourceStrategyFactory;
import com.soundhub.api.util.FileUtils;
import com.soundhub.api.util.mappers.UserMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
	@Value("${media.folder.avatars:avatars}")
	private String avatarFolderName;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FileService fileService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private MediaFileSourceStrategyFactory mediaFileSourceStrategyFactory;

	@Override
	public User addUser(UserDto userDto, MultipartFile file) throws IOException {
		String encodedPassword = passwordEncoder.encode(userDto.getPassword());
		String avatarUrl = fileService.uploadFile(avatarFolderName, file);

		User user = User.builder()
				.email(userDto.getEmail())
				.password(encodedPassword)
				.firstName(userDto.getFirstName())
				.lastName(userDto.getLastName())
				.birthday(userDto.getBirthday())
				.city(userDto.getCity())
				.country(userDto.getCountry())
				.gender(userDto.getGender())
				.avatarUrl(avatarUrl)
				.description(userDto.getDescription())
				.languages(userDto.getLanguages())
				.favoriteGenres(userDto.getFavoriteGenres())
				.favoriteArtistsMbids(userDto.getFavoriteArtistsMbids())
				.role(Role.USER)
				.build();

		user = userRepository.save(user);

		return user;
	}

	@Override
	public User addFriend(UUID friendId) {
		User user = getCurrentUser();
		User newFriend = getUserById(friendId);

		user.getFriends().add(newFriend);
		log.info("addFriend[1]: Friend added successfully ID {}", friendId);

		newFriend.getFriends().add(user);
		log.info("addFriend[2]: Friends list {}", user.getFriends());

		userRepository.save(user);
		userRepository.save(newFriend);

		return user;
	}

	@Override
	public User deleteFriend(UUID friendId) {
		User user = getCurrentUser();
		User delFriend = userRepository.findById(friendId)
				.orElseThrow(() -> new ResourceNotFoundException(
						Constants.USER_RESOURCE_NAME, Constants.ID_FIELD, friendId)
				);

		user.getFriends().remove(delFriend);
		updateUser(user.getId(), userMapper.userToUserDto(user));

		log.info("deleteFriend[1]: Friend deleted successfully ID {}", friendId);

		return user;
	}

	@Override
	public User getUserById(UUID id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(
						Constants.USER_RESOURCE_NAME, Constants.ID_FIELD, id)
				);
	}

	@Override
	public UUID deleteUser(UUID userId) throws IOException {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException(
						Constants.USER_RESOURCE_NAME, Constants.ID_FIELD, userId)
				);

		String fileName = user.getAvatarUrl();
		Files.deleteIfExists(FileUtils.getStaticFilePath(avatarFolderName, fileName));

		userRepository.delete(user);

		return user.getId();
	}

	@Override
	public UserDto updateUser(UUID userId, UserDto userDto) {
		User user = userRepository.findById(userId)
				.orElseThrow(() ->
						new ResourceNotFoundException(
								Constants.USER_RESOURCE_NAME, Constants.ID_FIELD, userId)
				);

		userMapper.updateUserFromDto(userDto, user);
		userRepository.save(user);

		return userMapper.userToUserDto(user);
	}

	@Override
	public UserDto updateUser(UUID userId, UserDto userDto, MultipartFile file) throws IOException {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException(
						Constants.USER_RESOURCE_NAME, Constants.ID_FIELD, userId)
				);

		String fileName = user.getAvatarUrl();

		if (file != null) {
			if (fileName != null) {
				MediaFileSourceStrategy strategy = mediaFileSourceStrategyFactory.getStrategy();
				strategy.deleteFile(avatarFolderName, fileName);
			}

			fileName = fileService.uploadFile(avatarFolderName, file);
		}

		userMapper.updateUserFromDto(userDto, user);
		user.setAvatarUrl(fileName);
		userRepository.save(user);

		return userMapper.userToUserDto(user);
	}

	@Override
	public List<User> getUsersByIds(List<UUID> ids) {
		return userRepository.findByUserIds(ids);
	}

	@Override
	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(
						() -> new ResourceNotFoundException(
								Constants.USER_RESOURCE_NAME, Constants.EMAIL_FIELD, email
						)
				);
	}

	@Override
	public Boolean checkEmailAvailability(String email) {
		return userRepository.existsByEmail(email);
	}

	@Override
	public UserExistenceResponse checkUserExistence(String email) {
		if (!email.matches(Constants.EMAIL_REGEX)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, Constants.INVALID_EMAIL);
		}

		boolean isUserExists = checkEmailAvailability(email);
		return new UserExistenceResponse(isUserExists);
	}

	@Override
	public User getCurrentUser() {
		String username = SecurityContextHolder.getContext()
				.getAuthentication()
				.getName();

		return getUserByEmail(username);
	}

	@Override
	public List<User> getUserFriendsById(UUID id) {
		log.info("getUserFriendsById[1]: getting user's: {} friends", id);
		User user = getUserById(id);

		log.info("getUserFriendsById[2]: user: {}", user);
		log.info("getUserFriendsById[3]: user's friends: {}", user.getFriends());

		return user.getFriends();
	}

	@Override
	public List<User> searchByFullName(String name) {
		log.info("searchByFullName[1]: searching users with name: {}", name);

		if (name.contains(" ")) {
			String[] parts = name.split("\\s+");
			String firstName = parts[0];
			String lastName = parts[1];

			return userRepository.searchByFirstNameAndLastName(firstName, lastName);
		}

		return userRepository.searchByFirstNameOrLastName(name, name);
	}

	@Override
	@Transactional
	public User updateUserOnline(boolean online) {
		User currentUser = getCurrentUser();
		boolean currentOnline = currentUser.isOnline();

		if (currentOnline == online)
			return currentUser;

		currentOnline = !currentOnline;
		currentUser.setOnline(currentOnline);

		LocalDateTime lastOnline = !currentOnline ? LocalDateTime.now() : null;

		currentUser.setLastOnline(lastOnline);

		userRepository.save(currentUser);
		return currentUser;
	}
}
