package com.soundhub.api.controllers;

import com.soundhub.api.services.FileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("api/v1/files")
@Slf4j
public class FileController {
	@Autowired
	private FileService fileService;

	@Value("${media.folder.static}")
	private String staticFolder;

	@PostMapping("/upload")
	public ResponseEntity<String> uploadFileHandler(@RequestPart MultipartFile file) throws IOException {
		log.debug("uploadFileHandler[1]: received file is {}", file.getOriginalFilename());

		String fileName = fileService.uploadFile(staticFolder, file);

		return ResponseEntity.ok("File was uploaded: " + fileName);
	}

	@GetMapping("/{filename}")
	public void getFile(
			@PathVariable String filename,
			@RequestParam String folderName,
			HttpServletResponse httpServletResponse
	) throws IOException {
		InputStream resourceFile = fileService.getFile(folderName, filename);

		httpServletResponse.setContentType(MediaType.ALL_VALUE);
		StreamUtils.copy(resourceFile, httpServletResponse.getOutputStream());
	}

	@PostMapping("/upload/files")
	public ResponseEntity<List<String>> uploadListFilesHandler(@RequestPart List<MultipartFile> files) {
		List<String> fileNames = fileService.uploadFileList(staticFolder, files);
		return ResponseEntity.ok(fileNames);
	}
}
