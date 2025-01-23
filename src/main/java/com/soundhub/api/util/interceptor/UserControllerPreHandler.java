package com.soundhub.api.util.interceptor;

import com.soundhub.api.controller.UserController;
import com.soundhub.api.dto.UserDto;
import com.soundhub.api.service.impl.FileUrlTransformer;
import com.soundhub.api.util.interceptor.transformer.url.UserTransformHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice(basePackageClasses = {UserController.class})
class UserControllerPreHandler extends AbstractResponseBodyAdvice<UserDto> {
    @Autowired
    private FileUrlTransformer urlTransformer;


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
            return body;
        }

        try {
            UserTransformHandler handler = new UserTransformHandler(urlTransformer);
            handler.transform(body);

        } catch (IllegalArgumentException e) {
            log.error("beforeBodyWrite[1]: {}", e.getMessage());
        }

        return body;
    }
}