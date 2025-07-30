package com.soundhub.api.services;

import com.soundhub.api.dto.response.CompatibleUsersResponse;

import java.util.List;
import java.util.UUID;

public interface UserCompatibilityService {
	CompatibleUsersResponse findCompatibilityPercentage(List<UUID> listUsersCompareWith);
}
