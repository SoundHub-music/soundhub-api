package com.soundhub.api.service;

import com.soundhub.api.exception.ApiException;
import com.soundhub.api.exception.ResourceNotFoundException;
import com.soundhub.api.service.impl.FileServiceImpl;
import com.soundhub.api.service.strategies.media.MediaFileSourceStrategy;
import com.soundhub.api.service.strategies.media.MediaFileSourceStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

	@Mock
	private MediaFileSourceStrategyFactory mediaFileSourceStrategyFactory;

	@Mock
	private MediaFileSourceStrategy strategy;

	@InjectMocks
	private FileServiceImpl fileService;

	@BeforeEach
	void setUp() {
		when(mediaFileSourceStrategyFactory.getStrategy()).thenReturn(strategy);
	}

	@Test
	void uploadFile_ShouldReturnFilename_WhenSuccessful() throws IOException {
		String path = "test/path";
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"example.txt",
				"text/plain",
				"content".getBytes()
		);
		when(strategy.uploadFile(path, file)).thenReturn("example.txt");

		String result = fileService.uploadFile(path, file);

		assertEquals("example.txt", result);
		verify(strategy, times(1)).uploadFile(path, file);
	}

	@Test
	void uploadFileList_ShouldReturnListOfFilenames_WhenAllSuccessful() {
		// Arrange
		String path = "upload";
		MultipartFile file1 = new MockMultipartFile(
				"file1", "a.txt", "text/plain", "a".getBytes()
		);

		MultipartFile file2 = new MockMultipartFile(
				"file2", "b.txt", "text/plain", "b".getBytes()
		);

		when(strategy.uploadFile(eq(path), any(MultipartFile.class)))
				.thenReturn("a.txt")
				.thenReturn("b.txt");

		List<String> names = fileService.uploadFileList(path, Arrays.asList(file1, file2));

		assertIterableEquals(Arrays.asList("a.txt", "b.txt"), names);
		verify(strategy, times(2)).uploadFile(eq(path), any(MultipartFile.class));
	}

	@Test
	void uploadFileList_ShouldThrowApiException_WhenIOExceptionOccurs() {
		String path = "upload";
		MultipartFile file = new MockMultipartFile(
				"file", "error.txt", "text/plain", "err".getBytes());

		when(strategy.uploadFile(path, file)).thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "IO failure"));

		ApiException ex = assertThrows(ApiException.class, () ->
				fileService.uploadFileList(path, List.of(file))
		);

		assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
		assertTrue(ex.getMessage().contains("IO failure"));
	}

	@Test
	void getFile_ShouldReturnInputStream_WhenFileExists() {
		String path = "dir";
		String filename = "file.txt";
		byte[] data = "hello".getBytes();

		InputStream mockStream = new ByteArrayInputStream(data);
		when(mediaFileSourceStrategyFactory.getStrategy()).thenReturn(strategy);
		when(strategy.getFile(getFullPath(path, filename))).thenReturn(mockStream);

		InputStream result = fileService.getFile(path, filename);

		assertNotNull(result);
		byte[] buffer = new byte[5];

		try {
			int read = result.read(buffer);
			assertEquals(5, read);
			assertArrayEquals(data, buffer);
		} catch (IOException e) {
			fail("Exception reading stream");
		}

		verify(strategy, times(1)).getFile(getFullPath(path, filename));
	}

	private String getFullPath(String path, String filename) {
		return Paths.get(path, filename).toString();
	}

	@Test
	void getFile_ShouldThrowResourceNotFoundException_WhenStrategyThrows() {
		String path = "dir";
		String filename = "nofile.txt";

		when(mediaFileSourceStrategyFactory.getStrategy()).thenReturn(strategy);
		when(strategy.getFile(anyString())).thenThrow(new RuntimeException("fail"));

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
				fileService.getFile(path, filename)
		);

		assertTrue(ex.getMessage().contains(filename));
	}
}
