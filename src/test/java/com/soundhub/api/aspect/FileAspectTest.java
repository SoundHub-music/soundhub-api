package com.soundhub.api.aspect;

import com.soundhub.api.Constants;
import com.soundhub.api.dto.PostDto;
import com.soundhub.api.model.Chat;
import com.soundhub.api.model.Post;
import com.soundhub.api.model.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest
@Slf4j
@TestPropertySource(properties = {
    "base.url=192.168.1.40:8080"
})
public class FileAspectTest {

    @Autowired
    private FileAspect fileAspect;

    @Value("${base.url:http://192.168.1.40:8080}")
    private String baseUrl;

    private User user;
    private Chat chat;
    private Post post;
    private PostDto postDto;

    @BeforeEach
    public void setup() {
        // Создаем фикстуры, такие как user, chat, post, postDto и устанавливаем значения URL для проверки
        user = new User();
        user.setAvatarUrl("someAvatar.jpg");

        post = new Post();
        post.setImages(List.of("image1.jpg", "https://existing.url/image2.jpg"));

        postDto = new PostDto();
        postDto.setImages(List.of("image3.jpg", "https://existing.url/image4.jpg"));

        chat = new Chat();
        chat.setParticipants(List.of(user));
    }

    @Test
    public void modifyUser_shouldModifyAvatarUrl() {
        fileAspect.modifyUser(user);
        log.debug("modifyUser_shouldModifyAvatarUrl[1]: modified avatar url is {}", user.getAvatarUrl());
        assertEquals(baseUrl + Constants.FILE_PATH_PART + "someAvatar.jpg", user.getAvatarUrl());
    }

    @Test
    public void modifyChat_shouldModifyAvatarUrlsInParticipants() {
        fileAspect.modifyChat(chat);
        chat.getParticipants().forEach(
                participant -> {
                    log.debug("modifyChat_shouldModifyAvatarUrlsInParticipants: new avatar is {}", participant.getAvatarUrl());
                    assertEquals(baseUrl + Constants.FILE_PATH_PART + "someAvatar.jpg", participant.getAvatarUrl());
                }
        );
    }

    @Test
    public void modifyPostUrl_shouldModifyPostImages() {
        fileAspect.modifyPostUrl(post);
        assertEquals(baseUrl + Constants.FILE_PATH_PART + "image1.jpg", post.getImages().get(0));
        assertEquals("https://existing.url/image2.jpg", post.getImages().get(1));
    }

    @Test
    public void modifyPostDto_shouldModifyPostDtoImages() {
        fileAspect.modifyPostDto(postDto);
        assertEquals(baseUrl + Constants.FILE_PATH_PART + "image3.jpg", postDto.getImages().get(0));
        assertEquals("https://existing.url/image4.jpg", postDto.getImages().get(1));
    }
}
