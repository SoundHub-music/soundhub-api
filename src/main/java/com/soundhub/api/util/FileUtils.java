package com.soundhub.api.util;

import com.soundhub.api.Constants;
import com.soundhub.api.exception.ApiException;
import com.soundhub.api.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class FileUtils {
    public static void createFolderIfNotExists(File folder) throws ApiException {
        if (!folder.exists()) {
            boolean isFolderCreated = folder.mkdir();

            if (!isFolderCreated) {
                throw new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        String.format(Constants.CREATE_STATIC_DIR_ERROR, folder)
                );
            }
        }
    }

    public static List<File> convertMultipartToFiles(List<MultipartFile> files) {
        return files.stream()
                .map(file -> {
                    try {
                        return file.getResource().getFile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    public static Path getStaticFilePath(String folder, String filename) {
        File staticFile = new File(folder);

        log.debug("getStaticFilePath[1]: {}", staticFile.getAbsolutePath());

        if (!staticFile.exists())
            throw new ResourceNotFoundException(String.format(Constants.FILE_NOT_FOUND, staticFile.getName()));

        return Paths.get(staticFile.getAbsolutePath(), filename);
    }
}
