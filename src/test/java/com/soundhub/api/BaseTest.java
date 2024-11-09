package com.soundhub.api;

import com.soundhub.api.dto.UserDto;
import com.soundhub.api.enums.Role;
import com.soundhub.api.model.Chat;
import com.soundhub.api.model.User;
import com.soundhub.api.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;

@SpringBootTest(properties = {
        "project.resources.path=src/test/resources",
        "project.staticFolder=static/"
})
public class BaseTest {
    protected final String fileFolder = "uploads";
    protected UUID chatId;
    protected UUID anotherChatId;
    protected UUID userId;
    protected UUID anotherUserId;
    protected User user;
    protected User anotherUser;
    protected UserDto userDto;
    protected Chat anotherChat;
    protected Chat chat;
    @Value("${project.resources.path}")
    protected String resourcesPath;

    @Value("${project.staticFolder}")
    protected String staticFolder;

    protected String accessToken, bearerToken;

    @Autowired
    private JwtService jwtService;

    protected void generateAccessToken(User user) {
        accessToken = jwtService.generateToken(user);
        bearerToken = "Bearer " + accessToken;
    }

    protected void initUserDto() {
        MultipartFile file = mock(MultipartFile.class);

        userDto = UserDto.builder()
                .id(userId)
                .email("vasya.pupkin@gmail.com")
                .password("testPassword")
                .firstName("Vasya")
                .avatarUrl(file.getOriginalFilename())
                .lastName("Pupkin")
                .birthday(LocalDate.of(2000, 5, 15))
                .build();
    }

    protected void initUser() {
        anotherUserId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = User.builder()
                .id(UUID.randomUUID())
                .email("vasya.pupkin@gmail.com")
                .password("testPassword")
                .firstName("Vasya")
                .lastName("Pupkin")
                .birthday(LocalDate.of(2000, 5, 15))
                .role(Role.USER)
                .build();

        anotherUser = User.builder()
                .id(UUID.randomUUID())
                .email("another.pupkin@gmail.com")
                .password("testPassword")
                .firstName("Oleg")
                .lastName("Pupkin")
                .birthday(LocalDate.of(2005, 5, 15))
                .role(Role.USER)
                .build();
    }

    protected void initChat() {
        chatId = UUID.randomUUID();
        anotherChatId = UUID.randomUUID();

        anotherChat = Chat.builder()
                .id(anotherChatId)
                .createdBy(anotherUser)
                .isGroup(false)
                .participants(List.of(anotherUser))
                .build();

        chat = Chat.builder()
                .id(chatId)
                .createdBy(user)
                .isGroup(false)
                .participants(List.of(user))
                .build();
    }
}
