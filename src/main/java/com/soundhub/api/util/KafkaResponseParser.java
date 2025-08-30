package com.soundhub.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.soundhub.api.exceptions.ApiException;
import com.soundhub.api.exceptions.KafkaResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class KafkaResponseParser {
	ObjectMapper objectMapper;

	KafkaResponseParser(@Autowired ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public <T> List<T> parseList(String json, Class<T> classType) {
		try {
			String innerJson = objectMapper.readValue(json, String.class);
			CollectionType collectionType = objectMapper
					.getTypeFactory()
					.constructCollectionType(List.class, classType);

			return objectMapper.readValue(innerJson, collectionType);
		} catch (JsonProcessingException e) {
			log.error("KafkaResponseParser.parseList[1]: error: {}", e.getMessage());
			return List.of();
		}
	}

	public KafkaResponseException parseException(String json) {
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
}
