package com.soundhub.api.controllers;

import com.soundhub.api.models.Genre;
import com.soundhub.api.models.User;
import com.soundhub.api.security.JwtService;
import com.soundhub.api.services.GenreService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class GenreControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@MockitoBean
	private GenreService genreService;

	@InjectMocks
	private GenreController genreController;

	private String jwtToken;
	private Genre rock, pop, metal;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(genreController).build();

		User mockUser = Mockito.mock(User.class);

		jwtToken = "Bearer " + jwtService.generateToken(mockUser);
		log.debug("setUp[1]: jwtToken {}", jwtToken);

		rock = Genre.builder()
				.id(UUID.randomUUID())
				.name("Rock")
				.pictureUrl("rock.png")
				.build();

		pop = Genre.builder()
				.id(UUID.randomUUID())
				.name("Pop")
				.pictureUrl("pop.png")
				.build();

		metal = Genre.builder()
				.id(UUID.randomUUID())
				.name("Metal")
				.pictureUrl("metal.png")
				.build();
	}

	@Test
	@WithMockUser
	public void testGetAllGenres() throws Exception {
		List<Genre> genreList = List.of(rock, pop, metal);
		when(genreService.getAllGenres()).thenReturn(genreList);

		MvcResult result = mockMvc.perform(get("/api/v1/genres"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(rock.getId().toString()))
				.andExpect(jsonPath("$[1].id").value(pop.getId().toString()))
				.andExpect(jsonPath("$[2].id").value(metal.getId().toString()))
				.andReturn();

		log.debug("testGetAllGenres[1]: response result = {}", result.getResponse().getContentAsString());

		verify(genreService, times(1)).getAllGenres();
	}

	@Test
	public void testGetEmptyGenres() throws Exception {
		when(genreService.getAllGenres()).thenReturn(new ArrayList<>());

		MvcResult result = mockMvc.perform(get("/api/v1/genres"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(0))
				.andReturn();

		log.debug("testGetEmptyGenres[2]: response result = {}", result.getResponse().getContentAsString());
		verify(genreService, times(1)).getAllGenres();
	}
}
