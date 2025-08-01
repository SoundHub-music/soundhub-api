package com.soundhub.api.services;

import com.soundhub.api.dto.UserDto;
import com.soundhub.api.dto.response.UserExistenceResponse;
import com.soundhub.api.models.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface UserService {
	User addUser(UserDto userDto, MultipartFile file) throws IOException;

	User addFriend(UUID friendId);

	User deleteFriend(UUID friendId) throws IOException;

	User getUserById(UUID id);

	UUID deleteUser(UUID userId) throws IOException;

	UserDto updateUser(UUID userId, UserDto userDto) throws IOException;

	UserDto updateUser(UUID userId, UserDto userDto, MultipartFile file) throws IOException;

	List<User> getUsersByIds(List<UUID> ids);

	User getUserByEmail(String email);

	Boolean checkEmailAvailability(String email);

	UserExistenceResponse checkUserExistence(String email);

	User getCurrentUser();

	List<User> getUserFriendsById(UUID id);

	List<User> searchByFullName(String name);

	User updateUserOnline(boolean online);
}