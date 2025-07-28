package com.soundhub.api.service;

import com.soundhub.api.model.User;

import java.util.List;

public interface RecommendationService {
	List<User> getRecommendedUsers();
}
