package com.soundhub.api.service.impl;

import com.soundhub.api.Constants;
import com.soundhub.api.exception.ApiException;
import com.soundhub.api.exception.ResourceNotFoundException;
import com.soundhub.api.service.FileService;
import com.soundhub.api.service.strategies.media.MediaFileSourceStrategy;
import com.soundhub.api.service.strategies.media.MediaFileSourceStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FileServiceImpl implements FileService {
	@Autowired
	private MediaFileSourceStrategyFactory mediaFileSourceStrategyFactory;

	@Override
	public String uploadFile(String path, MultipartFile file) throws IOException {
		MediaFileSourceStrategy strategy = mediaFileSourceStrategyFactory.getStrategy();

		return strategy.uploadFile(path, file);
	}

	@Override
	public List<String> uploadFileList(String path, List<MultipartFile> multipartFile) {
		List<String> names = new ArrayList<>();

		multipartFile.parallelStream()
				.forEach(file -> {
					try {
						String uploadedFileName = uploadFile(path, file);
						names.add(uploadedFileName);
					} catch (IOException e) {
						throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
					}
				});

		return names;
	}

	@Override
	public InputStream getFile(String path, String filename) {
		try {
			String fullPath = Paths.get(path, filename).toString();
			return mediaFileSourceStrategyFactory.getStrategy()
					.getFile(fullPath);
		} catch (Exception e) {
			throw new ResourceNotFoundException(String.format(Constants.FILE_NOT_FOUND, filename));
		}
	}
}
