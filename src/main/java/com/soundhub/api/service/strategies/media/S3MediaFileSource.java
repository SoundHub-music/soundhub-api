package com.soundhub.api.service.strategies.media;

import com.soundhub.api.Constants;
import com.soundhub.api.exception.ApiException;
import com.soundhub.api.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class S3MediaFileSource implements MediaFileSourceStrategy {
	private S3Client client;

	@Value("${s3.bucket.name}")
	private String bucketName;

	@Value("${s3.bucket.tenantId}")
	private String bucketTenantId;

	@Value("${s3.endpoint}")
	private String endpoint;

	@Value("${s3.key.secret}")
	private String secretKey;

	@Value("${s3.key.id}")
	private String keyId;

	@Value("${s3.region}")
	private String region;

	@PostConstruct
	private void initClient() {
		String accessKey = getAccessKey();
		AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
		StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

		URI endpointUri = URI.create(endpoint);

		this.client = S3Client.builder()
				.endpointOverride(endpointUri)
				.credentialsProvider(credentialsProvider)
				.serviceConfiguration(builder -> builder.pathStyleAccessEnabled(true))
				.region(Region.of(region))
				.build();
	}

	private String getAccessKey() {
		return String.format("%s:%s", bucketTenantId, keyId);
	}

	@Override
	public InputStream getFile(String path) {
		GetObjectRequest request = GetObjectRequest.builder()
				.bucket(bucketName)
				.key(path)
				.build();

		try {
			return client.getObject(request);
		} catch (NoSuchKeyException e) {
			String message = String.format(Constants.FILE_NOT_FOUND, path);

			throw new ResourceNotFoundException(message);
		} catch (NoSuchBucketException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	@Override
	public String uploadFile(String folder, MultipartFile file) {
		log.debug("uploadFile[1]: resources path: {}", folder);

		try (InputStream inputStream = file.getInputStream()) {
			Path path = Paths.get(folder, file.getOriginalFilename());

			PutObjectRequest request = PutObjectRequest.builder()
					.bucket(bucketName)
					.key(path.toString())
					.contentType(file.getContentType())
					.build();

			long fileSize = file.getSize();
			RequestBody body = RequestBody.fromInputStream(inputStream, fileSize);

			client.putObject(request, body);
		} catch (Exception e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}

		return file.getOriginalFilename();
	}

	@Override
	public void deleteFile(String folder, String fileName) {
		Path filePath = Paths.get(folder, fileName);

		DeleteObjectRequest request = DeleteObjectRequest.builder()
				.bucket(bucketName)
				.key(filePath.toString())
				.build();

		try {
			client.deleteObject(request);
		} catch (Exception e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
}
