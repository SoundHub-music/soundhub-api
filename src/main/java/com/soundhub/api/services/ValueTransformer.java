package com.soundhub.api.services;

import java.util.List;

public interface ValueTransformer<T> {
	T transformValue(T value);

	List<T> transformValues(List<T> values);
}
