package com.soundhub.api.util.interceptors;

import com.soundhub.api.controllers.PostController;
import com.soundhub.api.models.Post;
import com.soundhub.api.services.impl.FileUrlTransformer;
import com.soundhub.api.util.interceptors.url.transformers.PostTransformHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackageClasses = {PostController.class})
public class PostControllerPreHandler extends TransformUrlResponseBodyAdvice<Post> {
	PostControllerPreHandler(@Autowired FileUrlTransformer urlTransformer) {
		super(new PostTransformHandler(urlTransformer));
	}
}
