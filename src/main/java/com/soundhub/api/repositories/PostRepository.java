package com.soundhub.api.repositories;

import com.soundhub.api.models.Post;
import com.soundhub.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
	List<Post> findAllByAuthor(User user);
}
