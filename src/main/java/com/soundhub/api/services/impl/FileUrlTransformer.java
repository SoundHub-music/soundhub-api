package com.soundhub.api.services.impl;

import com.soundhub.api.Constants;
import com.soundhub.api.services.ValueTransformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileUrlTransformer implements ValueTransformer<String> {
	@Value("${host.url}")
	private String baseUrl;

	@Override
	public String transformValue(String url) {
		if (url == null || url.matches(Constants.HOST_REGEX)) {
			return url;
		}

		return baseUrl + Constants.FILE_PATH_PART + url;
	}

	@Override
	public List<String> transformValues(List<String> urls) {
		return urls.stream().map(this::transformValue).toList();
	}
}
