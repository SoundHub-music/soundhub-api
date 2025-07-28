package com.soundhub.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.soundhub.api.exception.ApiException;
import com.soundhub.api.model.User;
import com.soundhub.api.repository.UserRepository;
import com.soundhub.api.service.RecommendationService;
import com.soundhub.api.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.soundhub.api.Constants.REQUEST_TIMEOUT;
import static com.soundhub.api.Constants.SERVICE_IS_UNAVAILABLE;

@AllArgsConstructor
@Getter
class KafkaResponseException extends Throwable {
	private final String errorType;
	private final int statusCode;
	private final String detail;
}

@Service
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
	private final Map<String, CompletableFuture<List<UUID>>> pendingRequests = new ConcurrentHashMap<>();

	@Value("${spring.kafka.recommendation.request-topic}")
	String RECOMMENDATION_PRODUCER_TOPIC;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public List<User> getRecommendedUsers() {
		User currentUser = userService.getCurrentUser();
		UUID userId = currentUser.getId();

		log.info("recommendUsers[1]: searching friends for user with id: {}", userId);

		Set<UUID> friendIds = currentUser.getFriends()
				.stream()
				.map(User::getId)
				.collect(Collectors.toSet());

		String requestId = UUID.randomUUID().toString();
		CompletableFuture<List<UUID>> futureResponse = new CompletableFuture<>();

		pendingRequests.put(requestId, futureResponse);

		kafkaTemplate.send(
				MessageBuilder.withPayload(userId)
						.setHeader(KafkaHeaders.TOPIC, RECOMMENDATION_PRODUCER_TOPIC)
						.setHeader(KafkaHeaders.KEY, requestId)
						.build()
		);

		try {
			List<UUID> response = futureResponse.get(5, TimeUnit.SECONDS);

			List<UUID> potentialFriends = response.stream()
					.filter(id -> !friendIds.contains(id))
					.toList();

			return userRepository.findAllById(potentialFriends);

		} catch (TimeoutException e) {
			log.error("recommendUsers[2]: error: {}", e.getMessage());

			throw new ApiException(HttpStatus.REQUEST_TIMEOUT, REQUEST_TIMEOUT);
		} catch (InterruptedException | ExecutionException e) {
			log.error("recommendUsers[3]: error: {}", e.getMessage());

			Throwable cause = e.getCause();

			if (cause instanceof KafkaResponseException) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ((KafkaResponseException) cause).getDetail());
			}

			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, SERVICE_IS_UNAVAILABLE);
		} finally {
			pendingRequests.remove(requestId);
		}
	}

	@KafkaListener(
			topics = "${spring.kafka.error-topic}",
			groupId = "${spring.kafka.recommendation.group}"
	)
	private void handleRecommendationError(
			@Payload String payload,
			@Header(KafkaHeaders.CORRELATION_ID) String messageKey,
			@Header("origin_topic") String originalTopic
	) {
		if (!RECOMMENDATION_PRODUCER_TOPIC.equals(originalTopic)) {
			return;
		}

		log.error("handleRecommendationError[1]: payload: {}", payload);
		CompletableFuture<List<UUID>> futureResponse = pendingRequests.get(messageKey);

		KafkaResponseException exception = parseKafkaResponseException(payload);

		if (futureResponse != null) {
			futureResponse.completeExceptionally(exception);
		}
	}

	private KafkaResponseException parseKafkaResponseException(String json) {
		try {
			String cleanJson = json.trim();
			if (cleanJson.startsWith("\"") && cleanJson.endsWith("\"")) {
				cleanJson = cleanJson.substring(1, cleanJson.length() - 1);
			}

			cleanJson = cleanJson.replace("\\\"", "\"");

			JsonNode root = objectMapper.readTree(cleanJson);
			return new KafkaResponseException(
					root.path("error_type").asText(),
					root.path("status_code").asInt(),
					root.path("detail").asText()
			);
		} catch (JsonProcessingException e) {
			log.error("Failed to parse error JSON. Original: {}. Error: {}", json, e.getMessage());
			throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid error format: " + e.getMessage());
		}
	}

	@KafkaListener(
			topics = "${spring.kafka.recommendation.response-topic}",
			groupId = "${spring.kafka.recommendation.group}"
	)
	private void handleRecommendationResponse(
			@Payload String payload,
			@Header(KafkaHeaders.CORRELATION_ID) String messageKey
	) {
		log.debug("handleRecommendationResponse[1]: payload: {}", payload);

		CompletableFuture<List<UUID>> future = pendingRequests.get(messageKey);

		if (future != null) {
			List<UUID> parsedList = parseJsonToUuidList(payload);

			future.complete(parsedList);
		}

	}

	private List<UUID> parseJsonToUuidList(String json) {
		try {
			log.debug("parseJsonToUuidList[1]: json: {}", json);

			String innerJson = objectMapper.readValue(json, String.class);
			CollectionType collectionType = objectMapper
					.getTypeFactory()
					.constructCollectionType(List.class, UUID.class);

			return objectMapper.readValue(innerJson, collectionType);
		} catch (JsonProcessingException e) {
			log.error("parseJsonToUuidList[1]: error: {}", e.getMessage());
			return List.of();
		}
	}
}

