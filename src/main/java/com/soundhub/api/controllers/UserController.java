package com.soundhub.api.controllers;

import com.soundhub.api.Constants;
import com.soundhub.api.dto.UserDto;
import com.soundhub.api.dto.request.CompatibleUsersRequest;
import com.soundhub.api.dto.response.CompatibleUsersResponse;
import com.soundhub.api.dto.response.UserExistenceResponse;
import com.soundhub.api.models.User;
import com.soundhub.api.services.RecommendationService;
import com.soundhub.api.services.UserCompatibilityService;
import com.soundhub.api.services.UserService;
import com.soundhub.api.util.mappers.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
	@Autowired
	private UserService userService;

	@Autowired
	private UserCompatibilityService userCompatibilityService;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private RecommendationService recommendationService;

	@GetMapping("/{userId}")
	public ResponseEntity<UserDto> getUserById(@PathVariable UUID userId) {
		User user = userService.getUserById(userId);
		UserDto userDto = userMapper.userToUserDto(user);

		return new ResponseEntity<>(userDto, HttpStatus.OK);
	}

	@GetMapping("/checkUser/{email}")
	public ResponseEntity<UserExistenceResponse> checkEmailAvailability(@PathVariable String email) {
		UserExistenceResponse response = userService.checkUserExistence(email);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PutMapping("/update/{userId}")
	public ResponseEntity<UserDto> updateUser(
			@PathVariable UUID userId,
			@RequestPart UserDto userDto,
			@RequestPart(
					required = false,
					name = Constants.FILE_REQUEST_PART_ID
			) MultipartFile file
	) throws IOException {
		return ResponseEntity.ok(userService.updateUser(userId, userDto, file));
	}

	@DeleteMapping("/delete/{userId}")
	public ResponseEntity<UUID> deleteUser(@PathVariable UUID userId) throws IOException {
		return ResponseEntity.ok(userService.deleteUser(userId));
	}

	@GetMapping("/currentUser")
	public ResponseEntity<UserDto> getCurrentUser() {
		User currentUser = userService.getCurrentUser();
		return new ResponseEntity<>(userMapper.userToUserDto(currentUser), HttpStatus.OK);
	}

	@PutMapping("/addFriend/{friendId}")
	public ResponseEntity<UserDto> addFriend(@PathVariable UUID friendId) {
		User newFriend = userService.addFriend(friendId);
		UserDto newFriendDto = userMapper.userToUserDto(newFriend);

		return ResponseEntity.ok(newFriendDto);
	}

	@PutMapping("/deleteFriend/{friendId}")
	public ResponseEntity<UserDto> deleteFriend(@PathVariable UUID friendId) throws IOException {
		User user = userService.deleteFriend(friendId);
		UserDto userDto = userMapper.userToUserDto(user);

		return ResponseEntity.ok(userDto);
	}

	@GetMapping("/recommendedFriends")
	public ResponseEntity<List<UserDto>> getRecommendedFriends() {
		List<User> potentialFriends = recommendationService.getRecommendedUsers();
		List<UserDto> userDtos = potentialFriends.stream().map(userMapper::userToUserDto).toList();

		return new ResponseEntity<>(userDtos, HttpStatus.OK);
	}

	@GetMapping("/{userId}/friends")
	public ResponseEntity<List<UserDto>> getUserFriendsById(@PathVariable UUID userId) {
		List<User> friends = userService.getUserFriendsById(userId);
		List<UserDto> friendsDto = friends.stream().map(userMapper::userToUserDto).toList();

		return new ResponseEntity<>(friendsDto, HttpStatus.OK);
	}

	@GetMapping("/search")
	public ResponseEntity<List<UserDto>> searchUsersByFullName(@RequestParam String name) {
		List<User> users = userService.searchByFullName(name);
		List<UserDto> usersDtoList = users.stream().map(userMapper::userToUserDto).toList();

		if (users.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(usersDtoList, HttpStatus.OK);
	}

	@PutMapping("/user/online")
	public ResponseEntity<UserDto> updateUserOnline(@RequestParam(name = "value") boolean online) {
		User toggledUser = userService.updateUserOnline(online);
		UserDto toggledUserDto = userMapper.userToUserDto(toggledUser);

		return new ResponseEntity<>(toggledUserDto, HttpStatus.OK);
	}

	@PostMapping("/compatibleUsers")
	public ResponseEntity<CompatibleUsersResponse> findCompatibilityPercentage(@RequestBody CompatibleUsersRequest requestBody) {
		List<UUID> userIds = requestBody.getListUsersCompareWith();
		CompatibleUsersResponse response = userCompatibilityService.findCompatibilityPercentage(userIds);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}