package com.soundhub.api.services;

import com.soundhub.api.BaseTest;
import com.soundhub.api.dto.PostDto;
import com.soundhub.api.exceptions.ResourceNotFoundException;
import com.soundhub.api.models.Post;
import com.soundhub.api.models.User;
import com.soundhub.api.repositories.PostRepository;
import com.soundhub.api.services.impl.PostServiceImpl;
import com.soundhub.api.services.strategies.media.MediaFileSourceStrategyFactory;
import com.soundhub.api.util.mappers.PostMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest extends BaseTest {

	@InjectMocks
	private PostServiceImpl postService;

	@Mock
	private PostRepository postRepository;

	@Mock
	private UserService userService;

	@Mock
	private MediaFileSourceStrategyFactory mediaFileSourceStrategyFactory;

	@Mock
	private PostMapper postMapper;

	private UUID postId;
	private UUID authorId;
	private PostDto postDto;
	private Post post;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		postId = UUID.randomUUID();
		authorId = UUID.randomUUID();
		initUser();
		user.setId(authorId);

		postDto = PostDto.builder()
				.id(postId)
				.author(user)
				.images(new ArrayList<>())
				.content("Test content")
				.build();

		post = Post.builder()
				.id(postId)
				.author(user)
				.images(new ArrayList<>())
				.content("Test content")
				.likes(Set.of(user))
				.build();
	}

	@Test
	public void testAddPost_Positive() {
		when(userService.getCurrentUser()).thenReturn(user);
		when(postRepository.save(any(Post.class))).thenReturn(post);

		Post result = postService.addPost(postDto, null);

		assertEquals(post, result);
		verify(postRepository, times(1)).save(any(Post.class));
	}

	@Test
	public void testAddPost_Negative() {
		when(userService.getCurrentUser()).thenReturn(user);
		when(postRepository.save(any(Post.class))).thenThrow(new RuntimeException("Database error"));

		assertThrows(RuntimeException.class, () -> postService.addPost(postDto, null));
	}

	@Test
	public void testToggleLike_Positive() {
		when(postRepository.findById(postId)).thenReturn(Optional.of(post));
		when(postRepository.save(any(Post.class))).thenReturn(post);

		Post result = postService.toggleLike(postId, user);

		assertEquals(post, result);
		verify(postRepository, times(1)).save(any(Post.class));
	}

	@Test
	public void testToggleLike_Negative() {
		when(postRepository.findById(postId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> postService.toggleLike(postId, user));
	}

	@Test
	public void testGetPostById_Positive() {
		when(postRepository.findById(postId)).thenReturn(Optional.of(post));

		Post result = postService.getPostById(postId);

		assertEquals(post, result);
	}

	@Test
	public void testGetPostById_Negative() {
		when(postRepository.findById(postId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> postService.getPostById(postId));
	}

	@Test
	public void testDeletePost_Positive() {
		when(postRepository.findById(postId)).thenReturn(Optional.of(post));
		when(userService.getCurrentUser()).thenReturn(user);

		UUID result = postService.deletePost(postId);

		assertEquals(postId, result);
		verify(postRepository, times(1)).delete(any(Post.class));
	}

	@Test
	public void testDeletePost_Negative() {
		when(postRepository.findById(postId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> postService.deletePost(postId));
	}

	@Test
	public void testUpdatePost_Positive() {
		when(postRepository.findById(postId)).thenReturn(Optional.of(post));
		when(userService.getCurrentUser()).thenReturn(user);
		when(postService.updatePost(postId, postDto)).thenReturn(post);

		Post result = postService.updatePost(postId, postDto);

		assertEquals(post, result);
		verify(postRepository, times(1)).save(any(Post.class));
	}

	@Test
	public void testUpdatePost_Negative() {
		when(postRepository.findById(postId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> postService.updatePost(postId, postDto));
	}

	@Test
	public void testGetPostsByAuthor_Positive() {
		when(userService.getUserById(authorId)).thenReturn(user);
		when(postRepository.findAllByAuthor(any(User.class))).thenReturn(Collections.singletonList(post));

		List<Post> result = postService.getPostsByAuthor(authorId);

		assertEquals(1, result.size());
		assertEquals(post, result.get(0));
	}

	@Test
	public void testGetPostsByAuthor_Negative() {
		when(userService.getUserById(authorId)).thenReturn(user);
		when(postRepository.findAllByAuthor(any(User.class))).thenReturn(Collections.emptyList());

		List<Post> result = postService.getPostsByAuthor(authorId);

		assertEquals(0, result.size());
	}
}
