package com.soundhub.api.services.impl;

import com.soundhub.api.exceptions.ApiException;
import com.soundhub.api.exceptions.KafkaResponseException;
import com.soundhub.api.models.User;
import com.soundhub.api.repositories.UserRepository;
import com.soundhub.api.services.RecommendationService;
import com.soundhub.api.services.UserService;
import com.soundhub.api.util.KafkaResponseParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.soundhub.api.Constants.*;

@Service
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
	private final Map<String, CompletableFuture<List<UUID>>> pendingRequests = new ConcurrentHashMap<>();

	private final String RECOMMENDATION_PRODUCER_TOPIC;

	private final UserRepository userRepository;

	private final UserService userService;

	private final KafkaTemplate<String, String> kafkaTemplate;

	private final KafkaResponseParser kafkaResponseParser;

	public RecommendationServiceImpl(
			@Autowired UserRepository userRepository,
			@Autowired UserService userService,
			@Autowired KafkaTemplate<String, String> kafkaTemplate,
			@Autowired KafkaResponseParser kafkaResponseParser,

			@Value("${spring.kafka.recommendation.request-topic}")
			String topic
	) {
		this.userRepository = userRepository;
		this.userService = userService;
		this.kafkaTemplate = kafkaTemplate;
		this.kafkaResponseParser = kafkaResponseParser;
		this.RECOMMENDATION_PRODUCER_TOPIC = topic;
	}

	@Override
	public List<User> getRecommendedUsers() {
		User currentUser = userService.getCurrentUser();
		UUID userId = currentUser.getId();

		log.info("recommendUsers[1]: searching friends for user with id: {}", userId);

		Set<UUID> friendIds = currentUser.getFriends()
				.stream()
				.map(User::getId)
				.collect(Collectors.toSet());

		Pair<String, CompletableFuture<List<UUID>>> request = createCompletableFuturePair();

		String requestId = request.a;
		CompletableFuture<List<UUID>> futureResponse = request.b;

		try {
			kafkaTemplate.send(
					MessageBuilder.withPayload(userId)
							.setHeader(KafkaHeaders.TOPIC, RECOMMENDATION_PRODUCER_TOPIC)
							.setHeader(KafkaHeaders.KEY, requestId)
							.build()
			);
		} catch (KafkaException error) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, error.getMessage());
		}

		return getUsersFromTask(futureResponse, friendIds, requestId);
	}

	private Pair<String, CompletableFuture<List<UUID>>> createCompletableFuturePair() {
		String requestId = UUID.randomUUID().toString();
		CompletableFuture<List<UUID>> futureResponse = new CompletableFuture<>();

		pendingRequests.put(requestId, futureResponse);

		return new Pair<>(requestId, futureResponse);
	}

	private List<User> getUsersFromTask(CompletableFuture<List<UUID>> future, Set<UUID> friendIds, String requestId) {
		try {
			List<UUID> response = future.get(KAFKA_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			List<UUID> potentialFriends = response.stream()
					.filter(id -> !friendIds.contains(id))
					.toList();

			return userRepository.findAllById(potentialFriends);

		} catch (TimeoutException e) {
			log.error("recommendUsers[2]: error: {}", e.getMessage());

			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, REQUEST_TIMEOUT);
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
		KafkaResponseException exception = kafkaResponseParser.parseException(payload);

		Optional.ofNullable(pendingRequests.get(messageKey))
				.ifPresent(future -> future.completeExceptionally(exception));
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

		Optional.ofNullable(pendingRequests.get(messageKey))
				.ifPresent(future -> {
							List<UUID> parsedList = kafkaResponseParser.parseList(payload, UUID.class);

							future.complete(parsedList);
						}
				);
	}
}
