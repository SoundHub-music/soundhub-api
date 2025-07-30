package com.soundhub.api.util.interceptors.url.transformers;

import com.soundhub.api.models.Genre;
import com.soundhub.api.services.ValueTransformer;

import java.util.Collection;

public class GenreTransformHandler extends ObjectTransformHandler {
	private final ValueTransformer<String> valueTransformer;

	public GenreTransformHandler(ValueTransformer<String> valueTransformer) {
		this.valueTransformer = valueTransformer;
	}

	@Override
	public boolean supports(Object object) {
		if (object instanceof Collection<?> && ((Collection<?>) object).stream().allMatch(this::supports)) {
			return true;
		}

		return object instanceof Genre;
	}

	@Override
	protected void doTransform(Object object) throws IllegalArgumentException {
		Genre genre = (Genre) object;
		String url = valueTransformer.transformValue(genre.getPictureUrl());
		genre.setPictureUrl(url);
	}
}
