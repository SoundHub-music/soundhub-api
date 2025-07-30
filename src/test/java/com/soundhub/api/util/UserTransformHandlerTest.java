package com.soundhub.api.util;

import com.soundhub.api.model.Genre;
import com.soundhub.api.model.TransformableUser;
import com.soundhub.api.model.User;
import com.soundhub.api.service.ValueTransformer;
import com.soundhub.api.util.interceptors.url.transformers.UserTransformHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class UserTransformHandlerTest {
	private ValueTransformer<String> valueTransformerMock;
	private UserTransformHandler userTransformHandler;

	@BeforeEach
	void setUp() {
		valueTransformerMock = mock(ValueTransformer.class);
		userTransformHandler = new UserTransformHandler(valueTransformerMock);
	}

	@Test
	void supports_shouldReturnTrueForTransformableUser() {
		TransformableUser user = mock(TransformableUser.class);

		boolean result = userTransformHandler.supports(user);

		assertTrue(result);
	}

	@Test
	void supports_shouldReturnFalseForUnsupportedObject() {
		Object unsupportedObject = new Object();

		boolean result = userTransformHandler.supports(unsupportedObject);

		assertFalse(result);
	}

	@Test
	void supports_shouldReturnTrueForCollectionOfTransformableUsers() {
		TransformableUser user1 = mock(TransformableUser.class);
		TransformableUser user2 = mock(TransformableUser.class);
		List<TransformableUser> users = List.of(user1, user2);

		boolean result = userTransformHandler.supports(users);

		assertTrue(result);
	}

	@Test
	void doTransform_shouldTransformUserAndFriends() {
		TransformableUser user = mock(TransformableUser.class);
		User friend = mock(User.class);
		Genre genre = mock(Genre.class);
		String transformedUrl = "transformed-url";

		when(user.getFavoriteGenres()).thenReturn(List.of(genre));
		when(user.getAvatarUrl()).thenReturn("original-url");
		when(user.getFriends()).thenReturn(List.of(friend));
		when(valueTransformerMock.transformValue("original-url")).thenReturn(transformedUrl);

		userTransformHandler.transform(user);

		verify(user).setAvatarUrl(transformedUrl);
		verify(friend).getFavoriteGenres();
	}

	@Test
	void doTransform_shouldLogErrorWhenTransformingInvalidObject() {
		Object invalidObject = new Object();

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> userTransformHandler.transform(invalidObject)
		);

		assertEquals("Unsupported object type: " + invalidObject.getClass().getName(), exception.getMessage());
	}

	@Test
	void doTransform_shouldHandleGenreTransformation() {
		TransformableUser user = mock(TransformableUser.class);
		Genre genre = mock(Genre.class);

		when(user.getFavoriteGenres()).thenReturn(List.of(genre));
		when(user.getAvatarUrl()).thenReturn("url");
		when(user.getFriends()).thenReturn(List.of());
		when(valueTransformerMock.transformValue("url")).thenReturn("transformed-url");

		userTransformHandler.transform(user);

		verify(valueTransformerMock).transformValue("url");
		verify(user).setAvatarUrl("transformed-url");
	}
}
