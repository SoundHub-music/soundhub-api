package com.soundhub.api.service;

import com.soundhub.api.BaseTest;
import com.soundhub.api.Constants;
import com.soundhub.api.exception.ApiException;
import com.soundhub.api.exception.ResourceNotFoundException;
import com.soundhub.api.service.impl.FileServiceImpl;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest extends BaseTest {
    @Autowired
    private FileService fileService;

    @Mock
    private MultipartFile mockFile;

    private final String fileNameRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}_.+\\.[a-zA-Z0-9]+$";

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        fileService = new FileServiceImpl(resourcesPath, staticFolder);
        deleteUploadsFolder();
    }

    private void deleteUploadsFolder() throws IOException {
        Path staticPath = Paths.get(resourcesPath, staticFolder, fileFolder);
        File staticFolder = staticPath.toFile();
        if (staticFolder.exists()) {
            FileUtils.deleteDirectory(staticFolder);
        }
    }

    private void createUploadsFolder() throws IOException {
        Path staticPath = Paths.get(resourcesPath, staticFolder, fileFolder);
        File staticFolder = staticPath.toFile();
        if (!staticFolder.exists()) {
            assertTrue(staticFolder.mkdir());
        }
    }

    @Test
    void testResourcesPathNotNull() {
        assertNotNull(resourcesPath);
    }

    @Test
    void testStaticFolderNotNull() {
        assertNotNull(staticFolder);
    }

    @Test
    void testUploadFile_Positive() throws IOException {
        String fileName = "test.txt";
        byte[] content = "Hello, World!".getBytes();

        when(mockFile.getOriginalFilename()).thenReturn(fileName);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));

        String result = fileService.uploadFile(fileFolder, mockFile);

        // Проверяем, что файл был загружен с корректным именем
        assertTrue(result.matches(fileNameRegex));
        assertTrue(result.endsWith(fileName));

        // Проверяем, что файл действительно существует
        Path uploadedFilePath = Path.of(resourcesPath, staticFolder, fileFolder, result);
        assertTrue(Files.exists(uploadedFilePath));

        Files.delete(uploadedFilePath);
    }

    @Test
    void testUploadFile_Negative_CouldNotCreateFolder() throws IOException {
        when(mockFile.getOriginalFilename()).thenReturn("test.txt");
        when(mockFile.getInputStream()).thenThrow(new IOException("Mocked exception"));

        Exception exception = assertThrows(ApiException.class, () -> fileService.uploadFile(fileFolder, mockFile));

        assertEquals(Constants.SAVE_STATIC_ERROR, exception.getMessage());
    }

    @Test
    void testUploadFileList_Positive() throws IOException {
        List<MultipartFile> files = Collections.singletonList(mockFile);
        String fileName = "test.txt";

        when(mockFile.getOriginalFilename()).thenReturn(fileName);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("Hello, World!".getBytes()));

        List<String> result = fileService.uploadFileList(fileFolder, files);

        assertEquals(1, result.size());
        assertTrue(result.get(0).matches(fileNameRegex));
        assertTrue(result.get(0).endsWith(fileName));
    }

    @Test
    void testGetResourceFile_Positive() throws IOException {
        String filename = "test.txt";
        String content = "Hello, World!";
        Path filePath = Path.of(resourcesPath, staticFolder, fileFolder, filename);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, content);

        InputStream result = fileService.getResourceFile(fileFolder, filename);
        byte[] data = result.readAllBytes();

        assertEquals(content, new String(data));

        // Чистим после теста
        Files.delete(filePath);
    }

    @Test
    void testGetResourceFile_Negative_FileNotFound() throws IOException {
        String filename = "nonexistent.txt";
        createUploadsFolder();

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            fileService.getResourceFile(fileFolder, filename);
        });

        assertEquals(String.format("Resource File %s not found", filename), exception.getMessage());
    }

    @Test
    void testGetStaticFilePath_Negative_FolderNotFound() {
        String filename = "nonexistent.txt";

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            fileService.getStaticFilePath(fileFolder, filename);
        });

        assertEquals(String.format("Resource File %s not found", fileFolder), exception.getMessage());
    }
}
