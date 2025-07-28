package com.soundhub.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soundhub.api.BaseTest;
import com.soundhub.api.dto.UserCompatibilityDto;
import com.soundhub.api.dto.UserDto;
import com.soundhub.api.dto.request.CompatibleUsersRequest;
import com.soundhub.api.dto.response.CompatibleUsersResponse;
import com.soundhub.api.dto.response.UserExistenceResponse;
import com.soundhub.api.exception.ApiException;
import com.soundhub.api.exception.ResourceNotFoundException;
import com.soundhub.api.model.User;
import com.soundhub.api.repository.UserRepository;
import com.soundhub.api.service.RecommendationService;
import com.soundhub.api.service.UserCompatibilityService;
import com.soundhub.api.service.UserService;
import com.soundhub.api.util.mappers.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class UserControllerTest extends BaseTest {

	@Mock
	private UserService userService;

	@Mock
	private UserCompatibilityService userCompatibilityService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserMapper userMapper;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private KafkaTemplate<String, String> kafkaTemplate;

	@InjectMocks
	private UserController userController;

	@Mock
	private RecommendationService recommendationService;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		userId = UUID.randomUUID();
		user = User.builder()
				.id(userId)
				.email("vasya.pupkin@gmail.com")
				.password("testPassword")
				.firstName("Vasya")
				.avatarUrl("avatar.jpg")
				.lastName("Pupkin")
				.birthday(LocalDate.of(2000, 5, 15))
				.build();

		userDto = UserDto.builder()
				.id(userId)
				.email("vasya.pupkin@gmail.com")
				.password("testPassword")
				.firstName("Vasya")
				.avatarUrl("avatar.jpg")
				.lastName("Pupkin")
				.birthday(LocalDate.of(2000, 5, 15))
				.build();
	}

	@Test
	public void testCheckEmailAvailability_emailExists_returnTrue() {
		log.debug("testCheckEmailAvailability_emailExists_returnTrue[1]: start test");
		when(userService.checkUserExistence(user.getEmail())).thenReturn(new UserExistenceResponse(true));

		ResponseEntity<UserExistenceResponse> response = userController.checkEmailAvailability(user.getEmail());
		log.debug("testCheckEmailAvailability_emailExists_returnTrue[2]: response: {}", response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(Objects.requireNonNull(response.getBody()).isUserExists());

		verify(userService, times(1)).checkUserExistence(user.getEmail());
	}

	@Test
	public void testCheckEmailAvailability_emailDoesNotExist_returnFalse() {
		log.debug("testCheckEmailAvailability_emailDoesNotExist_returnFalse[1]: start test");
		when(userService.checkUserExistence(user.getEmail())).thenReturn(new UserExistenceResponse(false));

		ResponseEntity<UserExistenceResponse> response = userController.checkEmailAvailability(user.getEmail());
		log.debug("testCheckEmailAvailability_emailDoesNotExist_returnFalse[2]: response: {}", response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertFalse(Objects.requireNonNull(response.getBody()).isUserExists());

		verify(userService, times(1)).checkUserExistence(user.getEmail());
	}

	@Test
	public void testGetUser_returnUserDto() {
		log.debug("testGetUser_returnUserDto[1]: start test");
		when(userService.getUserById(userId)).thenReturn(user);
		when(userMapper.userToUserDto(user)).thenReturn(userDto);

		ResponseEntity<UserDto> response = userController.getUserById(userId);
		log.debug("testGetUser_returnUserDto[2]: response: {}", response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(userDto, response.getBody());

		verify(userService, times(1)).getUserById(userId);
		verify(userMapper, times(1)).userToUserDto(user);
	}

	@Test
	public void testGetUser_nonExistentUser_returnNotFound() {
		log.debug("testGetUser_nonExistentUser_returnNotFound[1]: start test");
		when(userService.getUserById(userId)).thenThrow(new ResourceNotFoundException("user", "userId", userId));

		assertThrows(ResourceNotFoundException.class, () -> {
			ResponseEntity<UserDto> response = userController.getUserById(userId);
			log.debug("testGetUser_nonExistentUser_returnNotFound[2]: response: {}", response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
			assertNull(response.getBody());
		});
		verify(userService, times(1)).getUserById(userId);
	}

	@Test
	public void testUpdateUserWithFile_returnUpdatedUser() throws IOException {
		log.debug("testUpdateUser_returnUpdatedUser[1]: start test");
		MultipartFile file = mock(MultipartFile.class);
		when(userService.updateUser(eq(userId), eq(userDto), eq(file))).thenReturn(userDto);

		ResponseEntity<UserDto> response = userController.updateUser(userId, userDto, file);
		log.debug("testUpdateUserWithFile_returnUpdatedUser[2]: response: {}", response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(userDto, response.getBody());

		verify(userService, times(1)).updateUser(eq(userId), eq(userDto), eq(file));
	}

	@Test
	public void testUpdateUserWithFile_returnUserNotFound() throws IOException {
		log.debug("testUpdateUserWithFile_returnUserNotFound[1]: start test");
		MultipartFile file = mock(MultipartFile.class);
		when(userService.updateUser(eq(userId), eq(userDto), eq(file))).thenThrow(new ResourceNotFoundException("User", "id", userId));

		assertThrows(ResourceNotFoundException.class, () -> {
			ResponseEntity<UserDto> response = userController.updateUser(userId, userDto, file);
			log.debug("testUpdateUserWithFile_returnUserNotFound[2]: response: {}", response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
			assertNull(response.getBody());
		});
		verify(userService, times(1)).updateUser(eq(userId), eq(userDto), eq(file));
	}

	@Test
	public void testUpdateUserWithoutFile_returnUpdatedUser() throws IOException {
		log.debug("testUpdateUserWithoutFile_returnUpdatedUser[1]: start test");
		when(userService.updateUser(eq(userId), eq(userDto), isNull())).thenReturn(userDto);

		ResponseEntity<UserDto> response = userController.updateUser(userId, userDto, null);
		log.debug("testUpdateUserWithoutFile_returnUpdatedUser[2]: response: {}", response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(userDto, response.getBody());

		verify(userService, times(1)).updateUser(eq(userId), eq(userDto), isNull());
	}

	@Test
	public void testDeleteUser_returnUserId() throws IOException {
		log.debug("testDeleteUser_returnUserId[1]: start test");
		when(userService.deleteUser(userId)).thenReturn(userId);

		ResponseEntity<UUID> response = userController.deleteUser(userId);
		log.debug("testDeleteUser_returnUserId[2]: response: {}", response);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(userId, response.getBody());
		verify(userService, times(1)).deleteUser(userId);
	}

	@Test
	public void testDeleteUser_returnNotFound() throws IOException {
		log.debug("testDeleteUser_returnNotFound[1]: start test");
		when(userService.deleteUser(userId)).thenThrow(new ResourceNotFoundException("user", "userId", userId));

		Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
			ResponseEntity<UUID> response = userController.deleteUser(userId);
			log.debug("testDeleteUser_returnNotFound[2]: response: {}", response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
			assertNull(response.getBody());
		});
		verify(userService, times(1)).deleteUser(userId);
	}

	@Test
	public void testGetCurrentUser_returnCurrentUser() {
		log.debug("testGetCurrentUser_returnCurrentUser[1]: start test");
		when(userService.getCurrentUser()).thenReturn(user);

		ResponseEntity<UserDto> response = userController.getCurrentUser();
		log.debug("testGetCurrentUser_returnCurrentUser[2]: response: {}", response);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(userMapper.userToUserDto(user), response.getBody());
		verify(userService, times(1)).getCurrentUser();
	}

	@Test
	public void testAddFriend() throws IOException {
		log.debug("testAddFriend[1]: start test");
		when(userService.addFriend(userId)).thenReturn(user);
		when(userMapper.userToUserDto(user)).thenReturn(userDto);

		ResponseEntity<UserDto> response = userController.addFriend(userId);
		log.debug("testAddFriend[2]: response: {}", response);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(userDto, response.getBody());
		verify(userService, times(1)).addFriend(userId);
	}

	@Test
	public void testDeleteFriend() throws IOException {
		log.debug("testDeleteFriend[1]: start test");
		when(userService.deleteFriend(userId)).thenReturn(user);
		when(userMapper.userToUserDto(user)).thenReturn(userDto);

		ResponseEntity<UserDto> response = userController.deleteFriend(userId);

		log.debug("testDeleteFriend[2]: response: {}", response);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(userDto, response.getBody());
		verify(userService, times(1)).deleteFriend(userId);
	}

	@Test
	public void testGetRecommendedFriends() {
		User recommendedUser1 = User.builder().id(UUID.randomUUID()).build();
		User recommendedUser2 = User.builder().id(UUID.randomUUID()).build();
		List<User> recommendedUsers = List.of(recommendedUser1, recommendedUser2);


		User currentUser = User.builder()
				.id(UUID.randomUUID())
				.friends(new ArrayList<>())
				.build();


		when(userService.getCurrentUser()).thenReturn(currentUser);
		when(recommendationService.getRecommendedUsers()).thenReturn(recommendedUsers);
		when(userMapper.userToUserDto(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			return UserDto.builder().id(user.getId()).build();
		});

		ResponseEntity<List<UserDto>> response = userController.getRecommendedFriends();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size());

		verify(recommendationService, times(1)).getRecommendedUsers();
		verify(userMapper, times(2)).userToUserDto(any(User.class));
	}

	@Test
	public void testGetUserFriendsById() {
		log.debug("testGetUserFriendsById[1]: start test");
		List<UserDto> friendsDto = List.of(userDto);
		List<User> friends = List.of(user);

		when(userService.getUserFriendsById(userId)).thenReturn(friends);
		when(userMapper.userToUserDto(user)).thenReturn(userDto);

		ResponseEntity<List<UserDto>> response = userController.getUserFriendsById(userId);

		log.debug("testGetUserFriendsById[2]: response: {}", response);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(friendsDto, response.getBody());
		verify(userService, times(1)).getUserFriendsById(userId);
	}

	@Test
	public void testSearchUsersByFullName() {
		log.debug("testSearchUsersByFullName[1]: start test");
		List<User> users = List.of(user);
		List<UserDto> usersDto = List.of(userDto);

		when(userService.searchByFullName("John")).thenReturn(users);
		when(userMapper.userToUserDto(user)).thenReturn(userDto);

		ResponseEntity<List<UserDto>> response = userController.searchUsersByFullName("John");
		log.debug("testSearchUsersByFullName[2]: response: {}", response);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(usersDto, response.getBody());
		verify(userService, times(1)).searchByFullName("John");
	}

	@Test
	public void testUpdateUserOnline() {
		log.debug("testToggleUserOnline[1]: start test");
		when(userService.updateUserOnline(true)).thenReturn(user);
		when(userMapper.userToUserDto(user)).thenReturn(userDto);

		ResponseEntity<UserDto> response = userController.updateUserOnline(true);
		log.debug("testToggleUserOnline[2]: response: {}", response);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(userDto, response.getBody());
		verify(userService, times(1)).updateUserOnline(true);
	}

	@Test
	public void testFindCompatibilityPercentage() {
		log.debug("testFindCompatibilityPercentage[1]: start test");
		List<UUID> userIds = List.of(userId);
		CompatibleUsersResponse compatibleUsersResponse = CompatibleUsersResponse.builder()
				.userCompatibilities(List.of(UserCompatibilityDto.builder()
						.user(user)
						.compatibility(95.5f)
						.build())).build();

		when(userCompatibilityService.findCompatibilityPercentage(userIds)).thenReturn(compatibleUsersResponse);

		ResponseEntity<CompatibleUsersResponse> response = userController.findCompatibilityPercentage(CompatibleUsersRequest.builder()
				.listUsersCompareWith(userIds).build());
		log.debug("testFindCompatibilityPercentage[2]: response: {}", response);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(compatibleUsersResponse, response.getBody());
		verify(userCompatibilityService, times(1)).findCompatibilityPercentage(userIds);
	}

	@Test
	public void testAddFriendNotFound() throws IOException {
		log.debug("testAddFriendNotFound[1]: start test");
		when(userService.addFriend(userId)).thenThrow(new ResourceNotFoundException("User", "id", userId));

		assertThrows(ResourceNotFoundException.class, () -> {
			ResponseEntity<UserDto> response = userController.addFriend(userId);
			log.debug("testAddFriendNotFound[2]: response: {}", response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
			assertNull(response.getBody());
		});

		verify(userService, times(1)).addFriend(userId);
	}

	@Test
	public void testDeleteFriendNotFound() throws IOException {
		log.debug("testDeleteFriendNotFound[1]: start test");
		when(userService.deleteFriend(userId)).thenThrow(new ResourceNotFoundException("User", "id", userId));

		Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
			ResponseEntity<UserDto> response = userController.deleteFriend(userId);
			log.debug("testDeleteFriendNotFound[2]: response: {}", response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
			assertNull(response.getBody());
		});

		verify(userService, times(1)).deleteFriend(userId);
	}

	@Test
	public void testGetRecommendedFriends_WhenNoRecommendations() {
		User currentUser = User.builder()
				.id(UUID.randomUUID())
				.friends(new ArrayList<>())
				.build();

		when(userService.getCurrentUser()).thenReturn(currentUser);
		when(recommendationService.getRecommendedUsers()).thenReturn(List.of());

		ResponseEntity<List<UserDto>> response = userController.getRecommendedFriends();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().isEmpty());

		verify(recommendationService, times(1)).getRecommendedUsers();
	}

	@Test
	public void testGetRecommendedFriends_WhenServiceUnavailable() {
		User currentUser = User.builder()
				.id(UUID.randomUUID())
				.friends(new ArrayList<>())
				.build();

		when(userService.getCurrentUser()).thenReturn(currentUser);
		when(recommendationService.getRecommendedUsers())
				.thenThrow(new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable"));

		assertThrows(ApiException.class, () -> {
			userController.getRecommendedFriends();
		});

		verify(recommendationService, times(1)).getRecommendedUsers();
	}

	@Test
	public void testGetUserFriendsByIdNotFound() {
		log.debug("testGetUserFriendsByIdNotFound[1]: start test");
		assertThrows(ResourceNotFoundException.class, () -> {
			when(userService.getUserFriendsById(userId)).thenThrow(new ResourceNotFoundException("User", "id", userId));

			ResponseEntity<List<UserDto>> response = userController.getUserFriendsById(userId);
			log.debug("testGetUserFriendsByIdNotFound[2]: response: {}", response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
			assertNull(response.getBody());
		});

		verify(userService, times(1)).getUserFriendsById(userId);
	}

	@Test
	public void testSearchUsersByFullNameNotFound() {
		log.debug("testSearchUsersByFullNameNotFound[1]: start test");
		when(userService.searchByFullName("NonExistentName")).thenReturn(new ArrayList<>());

		ResponseEntity<List<UserDto>> response = userController.searchUsersByFullName("NonExistentName");
		log.debug("testSearchUsersByFullNameNotFound[2]: response: {}", response);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull(response.getBody());
		verify(userService, times(1)).searchByFullName("NonExistentName");
	}

	@Test
	public void testUpdateUserOnlineNotFound() {
		log.debug("testToggleUserOnlineNotFound[1]: start test");
		assertThrows(ResourceNotFoundException.class, () -> {
			when(userService.updateUserOnline(true)).thenThrow(new ResourceNotFoundException("User", "id", userId));

			ResponseEntity<UserDto> response = userController.updateUserOnline(true);
			log.debug("testToggleUserOnlineNotFound[2]: response: {}", response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
			assertNull(response.getBody());
		});

		verify(userService, times(1)).updateUserOnline(true);
	}

	@Test
	public void testFindCompatibilityPercentageNotFound() {
		log.debug("testFindCompatibilityPercentageNotFound[1]: start test");
		List<UUID> userIds = List.of(userId);
		assertThrows(ResourceNotFoundException.class, () -> {
			when(userCompatibilityService.findCompatibilityPercentage(userIds)).thenThrow(new ResourceNotFoundException("User", "id", userId));

			ResponseEntity<CompatibleUsersResponse> response = userController.findCompatibilityPercentage(CompatibleUsersRequest.builder()
					.listUsersCompareWith(userIds).build());
			log.debug("testFindCompatibilityPercentageNotFound[2]: response: {}", response);
			assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
			assertNull(response.getBody());
		});

		verify(userCompatibilityService, times(1)).findCompatibilityPercentage(userIds);
	}
}
