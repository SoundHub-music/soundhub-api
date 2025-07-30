package com.soundhub.api.util.interceptor;

import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public abstract class AbstractResponseBodyAdvice<T> implements ResponseBodyAdvice<Object> {

	private final Class<T> targetType;

	protected AbstractResponseBodyAdvice() {
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
}
