package com.soundhub.api.services.media;

import com.soundhub.api.services.strategies.media.LocalMediaFileSource;
import com.soundhub.api.services.strategies.media.MediaFileSourceStrategy;
import com.soundhub.api.services.strategies.media.MediaFileSourceStrategyFactory;
import com.soundhub.api.services.strategies.media.S3MediaFileSource;
import com.soundhub.api.util.MediaSourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class MediaSourceStrategyFactoryTest {
	@MockitoBean
	private LocalMediaFileSource localStrategy;

	@MockitoBean
	private S3MediaFileSource s3Strategy;

	@Autowired
	private MediaFileSourceStrategyFactory mediaSourceStrategyFactory;

	@Test
	public void test_isS3Source() {
		MediaFileSourceStrategy strategy = mediaSourceStrategyFactory.getStrategy(MediaSourceType.S3);
		assertInstanceOf(S3MediaFileSource.class, strategy);
	}

	@Test
	public void test_isLocalSource() {
		MediaFileSourceStrategy strategy = mediaSourceStrategyFactory.getStrategy(MediaSourceType.LOCAL);
		assertInstanceOf(LocalMediaFileSource.class, strategy);
	}
}
