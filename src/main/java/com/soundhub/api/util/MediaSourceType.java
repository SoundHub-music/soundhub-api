package com.soundhub.api.util;

import lombok.Getter;

@Getter
public enum MediaSourceType {
    S3("s3"),
    LOCAL("local");

    private final String value;

    MediaSourceType(String value) {
        this.value = value;
    }
}
