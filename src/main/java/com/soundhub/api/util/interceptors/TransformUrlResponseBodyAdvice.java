package com.soundhub.api.util.interceptors;

import com.soundhub.api.util.interceptors.url.transformers.ObjectTransformHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

@Slf4j
public class TransformUrlResponseBodyAdvice<T> implements ResponseBodyAdvice<Object> {
	private final Class<T> targetType;
	private final ObjectTransformHandler objectTransformHandler;

	TransformUrlResponseBodyAdvice(ObjectTransformHandler valueTransformer) {
		this.objectTransformHandler = valueTransformer;
		this.targetType = getGenericType();
	}

	@SuppressWarnings("unchecked")
	private Class<T> getGenericType() {
		Type genericSuperclass = getClass().getGenericSuperclass();

		if (genericSuperclass instanceof ParameterizedType parameterizedType) {
			Type type = parameterizedType.getActualTypeArguments()[0];

			if (type instanceof Class<?>) {
				return (Class<T>) type;
			}
		}

		throw new IllegalStateException("Cannot determine generic type");
	}

	@Override
	public boolean supports(MethodParameter returnType, Class converterType) {
		Class<?> parameterType = returnType.getParameterType();
		Class<?> nestedParameterType = returnType.nested().getNestedParameterType();

		boolean isTargetType = targetType.isAssignableFrom(nestedParameterType);
		boolean isInResponse = ResponseEntity.class.isAssignableFrom(parameterType);

		if (Collection.class.isAssignableFrom(nestedParameterType)) {
			Class<?> listParameterType = returnType.nested().nested().getNestedParameterType();
			return targetType.isAssignableFrom(listParameterType) && isInResponse;
		}

		return isTargetType && isInResponse;
	}

	@Override
	public Object beforeBodyWrite(
			@Nullable Object body,
			MethodParameter returnType,
			MediaType selectedContentType,
			Class selectedConverterType,
			ServerHttpRequest request,
			ServerHttpResponse response
	) {
		if (body == null) {
			return null;
		}

		try {
			objectTransformHandler.transform(body);

		} catch (IllegalArgumentException e) {
			log.error("beforeBodyWrite[1]: {}", e.getMessage());
		}

		return body;
	}
}
