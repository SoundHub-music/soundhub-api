package com.soundhub.api.services.impl;

import com.soundhub.api.models.Genre;
import com.soundhub.api.repositories.GenreRepository;
import com.soundhub.api.services.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenreServiceImpl implements GenreService {
	@Autowired
	private GenreRepository genreRepository;

	@Override
	public List<Genre> getAllGenres() {
		return genreRepository.findAll();
	}
}
