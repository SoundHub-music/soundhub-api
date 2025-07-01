package com.soundhub.api.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileService {
    String uploadFile(String folder, MultipartFile file) throws IOException;

    List<String> uploadFileList(String folder, List<MultipartFile> multipartFile);

    InputStream getFile(String folder, String filename) throws FileNotFoundException;
}
