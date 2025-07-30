package com.soundhub.api.models;

import java.util.List;

public interface TransformableUser {
	String getAvatarUrl();

	void setAvatarUrl(String avatarUrl);

	List<User> getFriends();

	List<Genre> getFavoriteGenres();
}
