package com.soundhub.api.services;

import com.soundhub.api.dto.PostDto;
import com.soundhub.api.models.Post;
import com.soundhub.api.models.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface PostService {
	Post addPost(PostDto postDto, List<MultipartFile> files);

	Post toggleLike(UUID postId, User user);

	Post getPostById(UUID postId);

	UUID deletePost(UUID postId);

	Post updatePost(UUID postId, PostDto postDto);

	Post updatePost(
			UUID postId,
			PostDto postDto,
			List<MultipartFile> files,
			List<String> replaceFilesUrls
	) throws IOException;

	List<Post> getPostsByAuthor(UUID authorId);
}
