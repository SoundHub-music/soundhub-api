package com.soundhub.api.util.interceptors;

import com.soundhub.api.controller.UserController;
import com.soundhub.api.dto.UserDto;
import com.soundhub.api.service.impl.FileUrlTransformer;
import com.soundhub.api.util.interceptors.url.transformers.UserTransformHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice(basePackageClasses = {UserController.class})
class UserControllerPreHandler extends TransformUrlResponseBodyAdvice<UserDto> {
	UserControllerPreHandler(@Autowired FileUrlTransformer urlTransformer) {
		super(new UserTransformHandler(urlTransformer));
	}
}