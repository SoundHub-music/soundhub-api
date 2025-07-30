package com.soundhub.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soundhub.api.BaseTest;
import com.soundhub.api.dto.PostDto;
import com.soundhub.api.models.Post;
import com.soundhub.api.security.JwtService;
import com.soundhub.api.services.PostService;
import com.soundhub.api.services.UserService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class PostControllerTest extends BaseTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@MockitoBean
	private PostService postService;

	@MockitoBean
	private UserService userService;

	@InjectMocks
	private PostController postController;

	private UUID postId;
	private UUID authorId;
	private PostDto postDto;
	private Post postUpd;
	private Post post;
	private String jwtToken;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(postController).build();
		initUser();

		postId = UUID.randomUUID();
		authorId = UUID.randomUUID();


		postDto = PostDto.builder()
				.id(postId)
				.author(user)
				.content("Test content")
				.build();

		postUpd = Post.builder()
				.id(postId)
				.author(user)
				.content("Test content")
				.build();

		post = Post.builder()
				.id(postId)
				.author(user)
				.content("Test content")
				.build();

		jwtToken = "Bearer " + jwtService.generateToken(user);
		log.debug("setUp[1]: jwtToken {}", jwtToken);
	}


	@Test
	@WithMockUser
	public void testAddPost() throws Exception {
		MockMultipartFile postDtoFile = new MockMultipartFile("postDto", "", "application/json", new ObjectMapper().writeValueAsBytes(postDto));
		MockMultipartFile image1 = new MockMultipartFile("files", "image1.jpg", "image/jpeg", "image1 content".getBytes());
		MockMultipartFile image2 = new MockMultipartFile("files", "image2.jpg", "image/jpeg", "image2 content".getBytes());

		Mockito.when(postService.addPost(Mockito.any(PostDto.class), Mockito.anyList())).thenReturn(post);

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/posts/add")
						.file(postDtoFile)
						.file(image1)
						.file(image2)
						.header("Authorization", jwtToken)
						.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(postId.toString()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content").value(postDto.getContent()));

		verify(postService, times(1)).addPost(any(PostDto.class), anyList());
	}

	@Test
	@WithMockUser
	public void testGetPostByIdWithToken() throws Exception {
		when(postService.getPostById(postId)).thenReturn(post);

		mockMvc.perform(get("/api/v1/posts/{postId}", postId)
						.header("Authorization", jwtToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(postId.toString()))
				.andExpect(jsonPath("$.content").value("Test content"));
	}

	@Test
	public void testGetPostById() {
		when(postService.getPostById(postId)).thenReturn(post);

		ResponseEntity<Post> response = postController.getPostById(postId);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(post, response.getBody());

		verify(postService, times(1)).getPostById(postId);
	}

	@Test
	@WithMockUser
	public void testGetAllPostsByAuthor() throws Exception {
		when(postService.getPostsByAuthor(authorId)).thenReturn(Collections.singletonList(post));

		mockMvc.perform(get("/api/v1/posts/post/{authorId}", authorId)
						.header("Authorization", jwtToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(postId.toString()));
	}

	@Test
	@WithMockUser
	public void testUpdatePost() throws Exception {
		MockMultipartFile postDtoFile = new MockMultipartFile("postDto", "", "application/json", new ObjectMapper().writeValueAsBytes(postDto));
		MockMultipartFile image1 = new MockMultipartFile("files", "image1.jpg", "image/jpeg", "image1 content".getBytes());
		MockMultipartFile image2 = new MockMultipartFile("files", "image2.jpg", "image/jpeg", "image2 content".getBytes());

		when(postService.updatePost(any(), any(), any(), any())).thenReturn(postUpd);

		MockMultipartHttpServletRequestBuilder builder =
				MockMvcRequestBuilders.multipart("/api/v1/posts/update/{postId}", postId);

		builder.with(request -> {
			request.setMethod("PUT");
			return request;
		});

		MvcResult mvcResult = mockMvc.perform(builder
						.file(postDtoFile)
						.file(image1)
						.file(image2)
						.header("Authorization", "Bearer " + jwtToken)
						.contentType(MediaType.MULTIPART_FORM_DATA))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(postId.toString()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.content").value(postUpd.getContent()))
				.andReturn();
		log.debug("mvcResult \"{}\"", mvcResult.getResponse().getContentAsString());
	}

	@Test
	@WithMockUser
	public void testToggleLike() throws Exception {
		when(userService.getCurrentUser()).thenReturn(user);
		when(postService.toggleLike(postId, user)).thenReturn(post);

		mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/posts/like/{postId}", postId)
						.header("Authorization", jwtToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(postId.toString()));
	}

	@Test
	@WithMockUser
	public void testDeletePost() throws Exception {
		when(postService.deletePost(postId)).thenReturn(postId);

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/posts/delete/{postId}", postId)
						.header("Authorization", jwtToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(postId.toString()));
	}
}
