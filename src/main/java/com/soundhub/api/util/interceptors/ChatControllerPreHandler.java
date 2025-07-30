package com.soundhub.api.util.interceptors;

import com.soundhub.api.controllers.ChatController;
import com.soundhub.api.models.Chat;
import com.soundhub.api.services.impl.FileUrlTransformer;
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
