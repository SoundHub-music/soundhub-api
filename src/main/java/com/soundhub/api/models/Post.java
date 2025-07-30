package com.soundhub.api.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "posts")
@SuperBuilder
public class Post extends ContentEntity {
	@ElementCollection
	private List<String> images;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "user_posts",
			joinColumns = @JoinColumn(name = "post_id"),
			inverseJoinColumns = @JoinColumn(name = "user_id")
	)
	@Builder.Default
	private Set<User> likes = new HashSet<>();
}
