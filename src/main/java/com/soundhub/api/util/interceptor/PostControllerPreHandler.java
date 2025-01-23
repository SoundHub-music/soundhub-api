package com.soundhub.api.util.interceptor;

import com.soundhub.api.controller.PostController;
import com.soundhub.api.model.Post;
import com.soundhub.api.service.impl.FileUrlTransformer;
import com.soundhub.api.util.interceptor.transformer.url.PostTransformHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackageClasses = {PostController.class})
public class PostControllerPreHandler extends AbstractResponseBodyAdvice<Post> {
    @Autowired
    private FileUrlTransformer urlTransformer;

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (body == null) {
            return body;
        }

        try {
            PostTransformHandler handler = new PostTransformHandler(urlTransformer);
            handler.transform(body);
        } catch (IllegalArgumentException e) {
            log.error("beforeBodyWrite[1]: {}", e.getMessage());
        }

        return body;
    }
}
