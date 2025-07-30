package com.soundhub.api.services.strategies.media;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface MediaFileSourceStrategy {
	InputStream getFile(String path);

	String uploadFile(String folder, MultipartFile file);

	void deleteFile(String folder, String fileName);
}
