package com.soundhub.api.util.interceptors;

import com.soundhub.api.controllers.GenreController;
import com.soundhub.api.models.Genre;
import com.soundhub.api.services.impl.FileUrlTransformer;
import com.soundhub.api.util.interceptors.url.transformers.GenreTransformHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackageClasses = {GenreController.class})
public class GenreControllerPreHandler extends TransformUrlResponseBodyAdvice<Genre> {
	GenreControllerPreHandler(@Autowired FileUrlTransformer urlTransformer) {
		super(new GenreTransformHandler(urlTransformer));
	}
}
