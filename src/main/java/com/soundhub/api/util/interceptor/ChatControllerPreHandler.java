package com.soundhub.api.util.interceptor;

import com.soundhub.api.controller.ChatController;
import com.soundhub.api.model.Chat;
import com.soundhub.api.service.impl.FileUrlTransformer;
import com.soundhub.api.util.interceptor.transformer.url.ChatTransformHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackageClasses = {ChatController.class})
public class ChatControllerPreHandler extends AbstractResponseBodyAdvice<Chat> {
    @Autowired
    private FileUrlTransformer urlTransformer;


    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response
    ) {
        if (body == null) {
            return null;
        }

        try {
            ChatTransformHandler handler = new ChatTransformHandler(urlTransformer);
            handler.transform(body);
        } catch (IllegalArgumentException e) {
            log.error("beforeBodyWrite[1]: {}", e.getMessage());
        }

        return body;
    }
}
