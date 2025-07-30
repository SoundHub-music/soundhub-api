package com.soundhub.api.util.interceptors.url.transformers;

import com.soundhub.api.model.Chat;
import com.soundhub.api.model.User;
import com.soundhub.api.service.ValueTransformer;

import java.util.Collection;
import java.util.List;

public class ChatTransformHandler extends ObjectTransformHandler {
	private final ValueTransformer<String> valueTransformer;

	public ChatTransformHandler(ValueTransformer<String> valueTransformer) {
		this.valueTransformer = valueTransformer;
	}

	@Override
	boolean supports(Object object) {
		if (object instanceof Collection<?> && ((Collection<?>) object).stream().allMatch(this::supports)) {
			return true;
		}

		return object instanceof Chat;
	}

	@Override
	protected void doTransform(Object object) throws IllegalArgumentException {
		List<User> participants = ((Chat) object).getParticipants();
		UserTransformHandler handler = new UserTransformHandler(valueTransformer);

		participants.forEach(handler::transform);
	}
}
