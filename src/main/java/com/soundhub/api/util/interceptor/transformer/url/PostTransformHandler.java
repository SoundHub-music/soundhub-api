package com.soundhub.api.util.interceptor.transformer.url;

import com.soundhub.api.model.Post;
import com.soundhub.api.service.ValueTransformer;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

@Slf4j
public class PostTransformHandler extends ObjectTransformHandler {
    private final ValueTransformer<String> valueTransformer;

    public PostTransformHandler(ValueTransformer<String> valueTransformer) {
        this.valueTransformer = valueTransformer;
    }

    @Override
    boolean supports(Object object) {
        if (object instanceof Collection<?> && ((Collection<?>) object).stream().allMatch(this::supports)) {
            return true;
        }

        return object instanceof Post;
    }

    @Override
    protected void doTransform(Object object) throws IllegalArgumentException {
        UserTransformHandler handler = new UserTransformHandler(valueTransformer);

        try {
            Post post = (Post) object;

            List<String> imageUrls = valueTransformer.transformValues(post.getImages());

            handler.transform(post.getAuthor());
            post.setImages(imageUrls);
        } catch (IllegalArgumentException e) {
            log.error("doTransform[1]: {}", e.getMessage());
        }
    }
}
