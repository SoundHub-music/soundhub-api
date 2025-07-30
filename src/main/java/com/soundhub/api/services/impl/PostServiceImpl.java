package com.soundhub.api.services.impl;

import com.soundhub.api.Constants;
import com.soundhub.api.dto.PostDto;
import com.soundhub.api.exceptions.ApiException;
import com.soundhub.api.exceptions.ResourceNotFoundException;
import com.soundhub.api.models.Post;
import com.soundhub.api.models.User;
import com.soundhub.api.repositories.PostRepository;
import com.soundhub.api.services.FileService;
import com.soundhub.api.services.PostService;
import com.soundhub.api.services.UserService;
import com.soundhub.api.services.strategies.media.MediaFileSourceStrategy;
import com.soundhub.api.services.strategies.media.MediaFileSourceStrategyFactory;
import com.soundhub.api.util.mappers.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class PostServiceImpl implements PostService {
	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private FileService fileService;

	@Autowired
	private PostMapper postMapper;

	@Autowired
	private MediaFileSourceStrategyFactory mediaFileSourceFactory;

	@Value("${media.folder.posts}")
	private String basePostFolder;

	@Override
	public Post addPost(PostDto postDto, List<MultipartFile> files) {
		User author = userService.getCurrentUser();
		List<String> fileNames = new ArrayList<>();

		Post post = Post.builder()
				.author(author)
				.createdAt(LocalDateTime.now())
				.content(postDto.getContent())
				.images(fileNames)
				.build();

		log.info("addPost[1]: Adding post {}", post);

		post = postRepository.save(post);

		if (files != null && !files.isEmpty()) {
			String postFolderWithId = getPostFolder(post.getId());
			fileNames = fileService.uploadFileList(postFolderWithId, files);

			post.setImages(fileNames);

			return postRepository.save(post);
		}

		return post;
	}

	@Override
	public Post toggleLike(UUID postId, User user) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException(
						Constants.POST_RESOURCE_NAME, Constants.ID_FIELD, postId)
				);

		Set<User> likes = new HashSet<>(post.getLikes());
		boolean isChanged = likes.contains(user) ? likes.remove(user) : likes.add(user);

		log.info("toggleLike[1]: Toggled like successfully: {}", isChanged);

		if (isChanged) {
			postRepository.save(post);
		}

		return post;
	}

	@Override
	public Post getPostById(UUID postId) {
		log.info("getPostById[1]: Getting post by ID {}", postId);

		return postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException(
						Constants.POST_RESOURCE_NAME,
						Constants.ID_FIELD,
						postId
				));
	}

	@Override
	public UUID deletePost(UUID postId) {
		MediaFileSourceStrategy mediaSource = mediaFileSourceFactory.getStrategy();

		Post post = validatePostAuthorAndGetPostOrThrow(postId);
		String postFolder = getPostFolder(post.getId());

		List<String> postImages = post.getImages();
		log.info("deletePost[1]: Getting the post images urls {}", postImages);

		postImages.forEach(file -> mediaSource.deleteFile(postFolder, file));

		postRepository.delete(post);
		log.info("deletePost[2]: Images was successfully deleted from the disk. Post ID {} deleted", postId);

		return post.getId();
	}

	@Override
	@Transactional
	public Post updatePost(UUID postId, PostDto postDto) {
		Post post = validatePostAuthorAndGetPostOrThrow(postId);

		log.debug("updatePost[1]: Updating post without files replacing ID {}", postId);
		postMapper.updatePostFromDto(postDto, post);

		return postRepository.save(post);
	}

	@Override
	@Transactional
	public Post updatePost(
			UUID postId,
			PostDto postDto,
			List<MultipartFile> files,
			List<String> replaceFilesUrls
	) {
		Post post = validatePostAuthorAndGetPostOrThrow(postId);

		replaceFilesUrls = replaceFilesUrls.stream()
				.map(url -> url.substring(url.lastIndexOf('/') + 1))
				.toList();

		List<String> postImages = new ArrayList<>(post.getImages());

		List<String> newImages = addNewFiles(files, postId);
		postImages.addAll(newImages);

		deleteReplacingFiles(replaceFilesUrls, postImages, postId);
		postDto.setImages(postImages);

		log.debug("updatePost[1]: Updating post: files after insert {}", postImages);

		postMapper.updatePostFromDto(postDto, post);
		return postRepository.save(post);
	}

	private List<String> addNewFiles(List<MultipartFile> files, UUID postId) {
		if (files == null || files.isEmpty()) {
			return new ArrayList<>();
		}

		String postFolder = getPostFolder(postId);
		List<String> updatedFileNames = fileService.uploadFileList(postFolder, files);

		log.debug("addNewFiles[1]: Files added {} (if empty, no files to add)", updatedFileNames);
		return updatedFileNames;
	}

	private void deleteReplacingFiles(
			List<String> replaceFilesUrls,
			List<String> postImages,
			UUID postId
	) {
		if (replaceFilesUrls == null) {
			return;
		}

		MediaFileSourceStrategy mediaSource = mediaFileSourceFactory.getStrategy();

		replaceFilesUrls.forEach(file -> {
			String postFolder = getPostFolder(postId);

			mediaSource.deleteFile(postFolder, file);
			postImages.remove(file);

			log.debug("deleteReplacingFiles[1]: File deleted {}", file);
		});

		log.debug("deleteReplacingFiles[2]: Files remain {}", postImages);
	}

	@Override
	public List<Post> getPostsByAuthor(UUID authorId) {
		User user = userService.getUserById(authorId);
		log.info("getPostsByAuthor[1]: User entity was requested {}", user);

		return postRepository.findAllByAuthor(user);
	}

	private Post validatePostAuthorAndGetPostOrThrow(UUID postId) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException(
						Constants.POST_RESOURCE_NAME, Constants.ID_FIELD, postId)
				);

		User currentUser = userService.getCurrentUser();
		User postAuthor = post.getAuthor();

		if (!currentUser.equals(postAuthor)) {
			throw new ApiException(HttpStatus.FORBIDDEN, Constants.PERMISSION_MESSAGE);
		}

		return post;
	}

	private String getPostFolder(UUID postId) {
		return String.format("%s/%s", basePostFolder, postId.toString());
	}
}
