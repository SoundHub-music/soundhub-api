package com.soundhub.api.services.impl;

import com.soundhub.api.dto.UserCompatibilityDto;
import com.soundhub.api.dto.response.CompatibleUsersResponse;
import com.soundhub.api.models.Genre;
import com.soundhub.api.models.User;
import com.soundhub.api.services.UserCompatibilityService;
import com.soundhub.api.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class UserCompatibilityServiceImpl implements UserCompatibilityService {
	@Autowired
	private UserService userService;

	@Override
	public CompatibleUsersResponse findCompatibilityPercentage(List<UUID> listUsersCompareWith) {
		User userCompareTo = userService.getCurrentUser();
		List<User> usersCompareWith = userService.getUsersByIds(listUsersCompareWith);

		Map<User, Float> compatibilityMap = calculateCompatibilityMap(userCompareTo, usersCompareWith);

		List<UserCompatibilityDto> userCompatibilityList = convertToDtoList(compatibilityMap);

		return new CompatibleUsersResponse(userCompatibilityList);
	}

	private Map<User, Float> calculateCompatibilityMap(User userCompareTo, List<User> usersCompareWith) {
		Map<User, Float> compatibilityMap = new HashMap<>();

		List<UUID> artistsCompareTo = userCompareTo.getFavoriteArtistsMbids();
		List<UUID> genresCompareTo = extractGenreIds(userCompareTo.getFavoriteGenres());

		usersCompareWith.forEach(userCompareWith -> {
			List<UUID> artistsCompareWith = userCompareWith.getFavoriteArtistsMbids();
			List<UUID> genresCompareWith = extractGenreIds(userCompareWith.getFavoriteGenres());

			float artistCompatibility = calculateCompatibilityForUserBy(artistsCompareWith, artistsCompareTo);
			float genreCompatibility = calculateCompatibilityForUserBy(genresCompareWith, genresCompareTo);

			float meanCompatibility = calculateMeanCompatibility(artistCompatibility, genreCompatibility);

			if (meanCompatibility > 0) {
				compatibilityMap.put(userCompareWith, meanCompatibility);
			}
		});

		return compatibilityMap;
	}

	private List<UserCompatibilityDto> convertToDtoList(Map<User, Float> compatibilityMap) {
		return compatibilityMap.entrySet().stream()
				.map(entry -> UserCompatibilityDto.builder()
						.user(entry.getKey())
						.compatibility(entry.getValue())
						.build())
				.collect(Collectors.toList());
	}

	private List<UUID> extractGenreIds(List<Genre> genres) {
		return genres.stream()
				.map(Genre::getId)
				.toList();
	}

	private <T> float calculateCompatibilityForUserBy(List<T> entityCompareWith, List<T> entityCompareTo) {
		if (entityCompareTo.isEmpty() || entityCompareWith.isEmpty()) {
			return 0f;
		}

		Set<T> intersection = entityCompareWith.stream()
				.filter(entityCompareTo::contains)
				.collect(Collectors.toSet());

		Set<T> total = Stream.concat(entityCompareWith.stream(), entityCompareTo.stream())
				.collect(Collectors.toSet());

		return ((float) intersection.size() / (float) total.size()) * 100;
	}

	private float calculateMeanCompatibility(float artistCompatibility, float genreCompatibility) {
		if (artistCompatibility == 0 || genreCompatibility == 0) {
			return Math.max(artistCompatibility, genreCompatibility);
		}

		return (artistCompatibility + genreCompatibility) / 2;
	}
}