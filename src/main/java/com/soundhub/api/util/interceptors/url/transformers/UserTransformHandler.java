package com.soundhub.api.util.interceptors.url.transformers;

import com.soundhub.api.models.Genre;
import com.soundhub.api.models.TransformableUser;
import com.soundhub.api.models.User;
import com.soundhub.api.services.ValueTransformer;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class UserTransformHandler extends ObjectTransformHandler {
	private final ValueTransformer<String> valueTransformer;

	Set<TransformableUser> processedUsers = new HashSet<>();

	public UserTransformHandler(ValueTransformer<String> valueTransformer) {
		this.valueTransformer = valueTransformer;
	}

	@Override
	public boolean supports(Object object) {
		if (object instanceof Collection<?> && ((Collection<?>) object).stream().allMatch(this::supports)) {
			return true;
		}

		return object instanceof TransformableUser;
	}

	@Override
	protected void doTransform(Object object) throws IllegalArgumentException {
		GenreTransformHandler genreHandler = new GenreTransformHandler(valueTransformer);

		if (!(object instanceof TransformableUser user))
			throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());

		if (processedUsers.contains(user))
			return;


		try {
			List<Genre> genres = user.getFavoriteGenres();
			genreHandler.transform(genres);
		} catch (IllegalArgumentException e) {
			log.error("transform[1]: {}", e.getMessage());
		}

		String url = valueTransformer.transformValue(user.getAvatarUrl());
		user.setAvatarUrl(url);

		processedUsers.add(user);

		List<User> friends = user.getFriends();
		friends.forEach(this::transform);
	}
}
