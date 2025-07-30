package com.soundhub.api.services;

import com.soundhub.api.models.User;

import java.util.List;

public interface RecommendationService {
	List<User> getRecommendedUsers();
}
