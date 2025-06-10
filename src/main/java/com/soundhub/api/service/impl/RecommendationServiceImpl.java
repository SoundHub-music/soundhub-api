package com.soundhub.api.service.impl;

import com.soundhub.api.exception.ApiException;
import com.soundhub.api.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    @Value("${recommendation.url}")
    private String recommendationApi;

    @Override
    public List<UUID> getRecommendedUsers(UUID userId) {
        log.info("recommendUsers[1]: searching friends for user with id: {}", userId);

        if (recommendationApi == null) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Recommendation service is unavailable.");
        }

        String url = recommendationApi + "/" + userId;
        RestTemplate restTemplate = new RestTemplate();

        ParameterizedTypeReference<List<UUID>> responseType = new ParameterizedTypeReference<>() {
        };

        ResponseEntity<List<UUID>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                responseType
        );

        return response.getBody();
    }
}

