package com.soundhub.api.media_sources;

import com.soundhub.api.exceptions.ApiException;
import com.soundhub.api.exceptions.ResourceNotFoundException;
import com.soundhub.api.services.strategies.media.LocalMediaFileSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Slf4j
@TestPropertySource(
		properties = {
				"project.resources.path=test",
				"media.folder.static=static"
		}
)
@SpringBootTest
class LocalStrategyTest {
	private static final Path basePath = Path.of("test");

	@Autowired
	private LocalMediaFileSource localMediaFileSource;

	@Mock
	private MultipartFile mockFile;

	@AfterAll
	static void cleanUp() throws IOException {
		Files.walk(Path.of(basePath.toString()))
				.sorted(Comparator.reverseOrder())
				.map(Path::toFile)
				.forEach(File::delete);
	}

	@BeforeEach
	void setUp() throws IOException {
		MockitoAnnotations.openMocks(this);
		Files.createDirectories(basePath.resolve("static"));
		Files.createDirectories(basePath.resolve("static/delete"));
		Files.createDirectories(basePath.resolve("static/upload"));
	}

	@Test
	void testGetFile_Success() throws IOException {
		Path filePath = basePath.resolve("static/sample.txt");
		Path writtenFile = Files.writeString(filePath, "test");

		log.debug("written file is {}", writtenFile);
		InputStream result = localMediaFileSource.getFile("sample.txt");

		assertNotNull(result);
		assertEquals("test", new BufferedReader(new InputStreamReader(result)).readLine());
	}

	@Test
	void testGetFile_NotFound() {
		assertThrows(ResourceNotFoundException.class, () -> localMediaFileSource.getFile("not-exist.txt"));
	}

	@Test
	void testUploadFile_Success() throws IOException {
		when(mockFile.getOriginalFilename()).thenReturn("uploaded.txt");
		when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("hello world".getBytes()));

		String result = localMediaFileSource.uploadFile("upload", mockFile);

		assertEquals("uploaded.txt", result);

		Path expectedPath = basePath.resolve("static/upload/uploaded.txt");
		assertTrue(Files.exists(expectedPath), "Expected file not found at " + expectedPath);
	}


	@Test
	void testUploadFile_NullFile_Throws() {
		ApiException ex = assertThrows(ApiException.class,
				() -> localMediaFileSource.uploadFile("upload", null));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
	}

	@Test
	void testDeleteFile_Success() throws IOException {
		Path folder = basePath.resolve("static/delete");
		Path file = folder.resolve("temp.txt");

		Files.writeString(file, "to delete");

		assertTrue(Files.exists(file));
		localMediaFileSource.deleteFile("delete", "temp.txt");

		assertFalse(Files.exists(file));
	}

	@Test
	void testDeleteFile_NotExistFolder() {
		assertThrows(ResourceNotFoundException.class, () ->
				localMediaFileSource.deleteFile("ghost", "ghost.txt"));
	}
}
