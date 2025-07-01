package com.soundhub.api.service.strategies.media;

import com.soundhub.api.util.MediaSourceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MediaFileSourceStrategyFactory {
	@Autowired
	private LocalMediaFileSource localStrategy;

	@Autowired
	private S3MediaFileSource s3Strategy;

	@Value("${media.source}")
	private String mediaSourceType;

	public MediaFileSourceStrategy getStrategy() {
		log.debug("MediaFileSourceStrategyFactory[getStrategy]: {}", mediaSourceType);
		MediaSourceType type = MediaSourceType.valueOf(mediaSourceType.toUpperCase());

		return getStrategy(type);
	}

	public MediaFileSourceStrategy getStrategy(MediaSourceType type) {
		return switch (type) {
			case LOCAL -> localStrategy;
			case S3 -> s3Strategy;
			default -> throw new RuntimeException("Unknown media source type");
		};
	}
}
