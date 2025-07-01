package com.soundhub.api.media_sources;

import com.soundhub.api.exception.ApiException;
import com.soundhub.api.exception.ResourceNotFoundException;
import com.soundhub.api.service.strategies.media.S3MediaFileSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class S3StrategyTest {
	@InjectMocks
	private S3MediaFileSource s3MediaFileSource;

	@Mock
	private S3Client mockClient;

	@Mock
	private MultipartFile mockFile;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testGetFile_Success() {
		String key = "media/audio.mp3";
		ResponseInputStream<GetObjectResponse> responseStream = mock(ResponseInputStream.class);
		when(mockClient.getObject(any(GetObjectRequest.class))).thenReturn(responseStream);

		InputStream result = s3MediaFileSource.getFile(key);

		assertEquals(responseStream, result);
		verify(mockClient).getObject(any(GetObjectRequest.class));
	}

	@Test
	void testGetFile_NoSuchKey() {
		when(mockClient.getObject(any(GetObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());

		assertThrows(ResourceNotFoundException.class, () -> s3MediaFileSource.getFile("bad/file.mp3"));
	}

	@Test
	void testUploadFile_Success() throws IOException {
		when(mockFile.getOriginalFilename()).thenReturn("song.mp3");
		when(mockFile.getInputStream()).thenReturn(mock(InputStream.class));
		when(mockClient.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());

		String result = s3MediaFileSource.uploadFile("folder", mockFile);

		assertEquals("song.mp3", result);
		verify(mockClient).putObject(any(PutObjectRequest.class), any(RequestBody.class));
	}

	@Test
	void testUploadFile_Failure() {
		doThrow(RuntimeException.class).when(mockClient).putObject(any(PutObjectRequest.class), any(RequestBody.class));

		ApiException ex = assertThrows(ApiException.class, () ->
				s3MediaFileSource.uploadFile("folder", mockFile));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
	}

	@Test
	void testDeleteFile_Success() {
		DeleteObjectResponse mockResponse = DeleteObjectResponse.builder().deleteMarker(true).build();
		when(mockClient.deleteObject(any(DeleteObjectRequest.class))).thenReturn(mockResponse);
		s3MediaFileSource.deleteFile("folder", "file.mp3");

		verify(mockClient).deleteObject(any(DeleteObjectRequest.class));
	}


	@Test
	void testDeleteFile_Exception() {
		when(mockClient.deleteObject(any(DeleteObjectRequest.class))).thenThrow(RuntimeException.class);

		ApiException ex = assertThrows(ApiException.class, () ->
				s3MediaFileSource.deleteFile("folder", "file.mp3")
		);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getHttpStatus());
	}
}
