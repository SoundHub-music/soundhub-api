package com.soundhub.api.util.interceptor.transformer.url;

import org.springframework.lang.Nullable;

import java.util.Collection;

public abstract class ObjectTransformHandler {
    abstract boolean supports(Object object);

    public void transform(@Nullable Object object) throws IllegalArgumentException {
        if (object == null)
            return;

        if (object instanceof Collection<?>) {
            ((Collection<?>) object).forEach(this::transform);
            return;
        }

        if (!supports(object)) {
            throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
        }

        doTransform(object);
    }

    protected abstract void doTransform(Object object) throws IllegalArgumentException;
}
