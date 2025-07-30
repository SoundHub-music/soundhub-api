package com.soundhub.api.controllers;

import com.soundhub.api.models.Genre;
import com.soundhub.api.services.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/genres")
public class GenreController {
	@Autowired
	private GenreService genreService;

	@GetMapping
	public ResponseEntity<List<Genre>> getAllGenres() {
		return new ResponseEntity<>(genreService.getAllGenres(), HttpStatus.OK);
	}
}
