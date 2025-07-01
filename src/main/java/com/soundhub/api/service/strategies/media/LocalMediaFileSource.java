package com.soundhub.api.service.strategies.media;

import com.soundhub.api.Constants;
import com.soundhub.api.exception.ApiException;
import com.soundhub.api.exception.ResourceNotFoundException;
import com.soundhub.api.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
@Service
public class LocalMediaFileSource implements MediaFileSourceStrategy {
	@Value("${project.resources.path}")
	private String resourcesPath;

	@Value("${media.folder.static}")
	private String staticFolder;

	@Override
	public InputStream getFile(String path) {
		try {
			Path staticFilePath = Paths.get(resourcesPath, staticFolder, path);
			File staticFile = staticFilePath.toFile();

			if (!staticFile.exists()) {
				String message = String.format(Constants.FILE_NOT_FOUND, path);

				throw new ResourceNotFoundException(message);
			}

			return new FileInputStream(staticFile);
		} catch (FileNotFoundException exception) {
			String message = String.format(Constants.FILE_NOT_FOUND, path);

			throw new ResourceNotFoundException(message);
		}
	}

	@Override
	public String uploadFile(String folder, MultipartFile file) {
		log.debug("LocaleMediaFileSource.upload[1]: multipart file is {}", file);

		if (file == null || folder == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, Constants.INVALID_MULTIPART);
		}

		String fileName = file.getOriginalFilename();

		File fileFolder = getStaticPath(folder).toFile();
		File staticResourcesPath = Paths.get(resourcesPath, staticFolder).toFile();

		if (Objects.equals(folder, staticFolder)) {
			fileFolder = staticResourcesPath;
		}

		Path filePath = Paths.get(fileFolder.getAbsolutePath(), fileName);
		log.debug("uploadFile[1]: resources path: {}", resourcesPath);
		log.debug("uploadFile[2]: static folder: {}", staticFolder);

		FileUtils.createFolderIfNotExists(staticResourcesPath);
		FileUtils.createFolderIfNotExists(fileFolder);

		log.debug("uploadFile[1]: {}", resourcesPath);
		log.debug("uploadFile[2]: {}", fileFolder);

		try {
			Files.copy(file.getInputStream(), filePath);

			return fileName;
		} catch (IOException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	@Override
	public void deleteFile(String folder, String fileName) {
		try {
			File staticFolder = getStaticPath(folder).toFile();

			if (!staticFolder.exists()) {
				throw new ResourceNotFoundException(String.format(Constants.FILE_NOT_FOUND, staticFolder.getName()));
			}

			Path fullFilePath = Paths.get(staticFolder.getAbsolutePath(), fileName);

			Files.deleteIfExists(fullFilePath);
		} catch (IOException exception) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getLocalizedMessage());
		}
	}

	public Path getStaticPath(String path) {
		return Paths.get(resourcesPath, staticFolder, path);
	}
}
