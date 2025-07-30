package com.soundhub.api.util.interceptors;

import com.soundhub.api.controller.ChatController;
import com.soundhub.api.model.Chat;
import com.soundhub.api.service.impl.FileUrlTransformer;
import com.soundhub.api.util.interceptors.url.transformers.ChatTransformHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackageClasses = {ChatController.class})
public class ChatControllerPreHandler extends TransformUrlResponseBodyAdvice<Chat> {
	ChatControllerPreHandler(@Autowired FileUrlTransformer urlTransformer) {
		super(new ChatTransformHandler(urlTransformer));
	}
}
