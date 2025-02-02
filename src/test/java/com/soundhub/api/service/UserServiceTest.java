package com.soundhub.api.service;

import com.soundhub.api.BaseTest;
import com.soundhub.api.dto.UserDto;
import com.soundhub.api.dto.response.UserExistenceResponse;
import com.soundhub.api.exception.ResourceNotFoundException;
import com.soundhub.api.model.User;
import com.soundhub.api.repository.UserRepository;
import com.soundhub.api.service.impl.UserServiceImpl;
import com.soundhub.api.util.mappers.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(
        locations = "classpath:application.properties"
)
public class UserServiceTest extends BaseTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private FileService fileService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        initUser();
        initUserDto();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    public void testCheckUserExistence_userExists_returnTrue() {
        when(userService.checkEmailAvailability(user.getEmail())).thenReturn(true);

        UserExistenceResponse response = userService.checkUserExistence(user.getEmail());

        assertTrue(response.isUserExists());
    }

    @Test
    public void testCheckUserExistence_userDoesNotExist_returnFalse() {
        when(userService.checkEmailAvailability(user.getEmail())).thenReturn(false);

        UserExistenceResponse response = userService.checkUserExistence(user.getEmail());

        assertFalse(response.isUserExists());
    }

    @Test
    public void testAddUser() throws IOException {
        MultipartFile file = mock(MultipartFile.class);

        when(fileService.uploadFile(any(String.class), eq(file))).thenReturn(userDto.getAvatarUrl());
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encoded_password");

        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.addUser(userDto, file);

        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getPassword(), result.getPassword());
        assertEquals(user.getAvatarUrl(), result.getAvatarUrl());

        verify(userRepository).save(any(User.class));
        verify(fileService).uploadFile(any(String.class), eq(file));
    }

    @Test
    public void testAddFriend() {
        User friend = new User();
        friend.setId(UUID.randomUUID());

        when(userRepository.findById(friend.getId())).thenReturn(Optional.of(friend));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User result = userService.addFriend(friend.getId());

        // Проверка, что друг добавлен в список друзей
        assertTrue(result.getFriends().contains(friend));
        assertTrue(friend.getFriends().contains(user));

        // Проверка, что метод save вызывается дважды для обновления пользователей
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    public void testDeleteFriend() {
        User friend = new User();
        friend.setId(UUID.randomUUID());

        user.setFriends(new ArrayList<>());
        user.getFriends().add(friend);

        when(userRepository.findById(friend.getId())).thenReturn(Optional.of(friend));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User result = userService.deleteFriend(friend.getId());
        assertFalse(result.getFriends().contains(friend));
        verify(userRepository, times(1)).save(user);
    }


    @Test
    public void testGetUserById() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        User result = userService.getUserById(user.getId());
        assertEquals(user, result);
    }

    @Test
    public void testGetUserByIdNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    public void testGetCurrentUser() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(user.getEmail());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User result = userService.getCurrentUser();

        assertEquals(user, result);
    }

    @Test
    public void testDeleteUser() throws IOException {
        user.setAvatarUrl("avatar_url");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(fileService.getStaticFilePath(any(String.class), eq("avatar_url")))
                .thenReturn(Files.createTempFile("test", ".jpg"));

        UUID result = userService.deleteUser(user.getId());

        assertEquals(user.getId(), result);

        verify(userRepository).delete(user);
    }

    @Test
    public void testUpdateUser() {
        UserDto userDto = new UserDto();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.userToUserDto(user)).thenReturn(userDto);

        UserDto result = userService.updateUser(user.getId(), userDto);

        assertEquals(userDto, result);

        verify(userRepository).save(user);
    }

    @Test
    public void testUpdateUserOnline() {
        User result = userService.getCurrentUser();
        assertEquals(user, result);

        boolean isOnline = user.isOnline();
        User toggledUser = userService.updateUserOnline(!isOnline);

        assertNotEquals(isOnline, toggledUser.isOnline());
    }
}
