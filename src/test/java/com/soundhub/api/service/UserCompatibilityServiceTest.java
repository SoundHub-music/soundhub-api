package com.soundhub.api.service;

import com.soundhub.api.BaseTest;
import com.soundhub.api.dto.UserCompatibilityDto;
import com.soundhub.api.dto.response.CompatibleUsersResponse;
import com.soundhub.api.model.User;
import com.soundhub.api.service.impl.UserCompatibilityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class UserCompatibilityServiceTest extends BaseTest {
    @InjectMocks
    private UserCompatibilityServiceImpl userCompatibilityService;

    @Mock
    private UserService userService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        initUser();
        initUserDto();

        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(userService.getCurrentUser()).thenReturn(user);
    }

    @Test
    public void testFindCompatibilityPercentage() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        user.setFavoriteArtistsMbids(List.of(id1, id2, id3));

        User otherUser = new User();
        otherUser.setFavoriteArtistsMbids(List.of(id3, UUID.randomUUID(), UUID.randomUUID()));
        
        List<UUID> userIds = List.of(UUID.randomUUID());

        User actual = userService.getCurrentUser();
        assertEquals(user, actual);
        when(userService.getUsersByIds(userIds)).thenReturn(List.of(otherUser));

        CompatibleUsersResponse response = userCompatibilityService.findCompatibilityPercentage(userIds);

        assertNotNull(response);
        assertEquals(1, response.getUserCompatibilities().size());

        UserCompatibilityDto compatibilityDto = response.getUserCompatibilities().get(0);
        assertEquals(otherUser, compatibilityDto.getUser());
        assertEquals(20f, compatibilityDto.getCompatibility(), 0.01);
    }
}
