package com.soundhub.api.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.soundhub.api.Constants;
import com.soundhub.api.enums.Gender;
import com.soundhub.api.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Builder
public class User implements UserDetails, TransformableUser {
	@Id
	@GeneratedValue
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	@Column(name = "id")
	private UUID id;

	@NotBlank
	@Column(name = "email", unique = true)
	private String email;

	@NotBlank
	@Column(name = "password")
	private String password;

	@NotBlank
	@Column(name = "first_name")
	private String firstName;

	@NotBlank
	@Column(name = "last_name")
	private String lastName;

	@NotNull
	@Column(name = "birthday")
	@JsonSerialize(using = LocalDateSerializer.class)
	private LocalDate birthday;

	@Column(name = "city")
	private String city;

	@Column(name = "country")
	private String country;

	@Enumerated(EnumType.STRING)
	@Column(name = "gender")
	private Gender gender;

	@Column(name = "avatar_url")
	private String avatarUrl;

	@Column(name = "description")
	private String description;

	@ElementCollection(fetch = FetchType.EAGER)
	@Builder.Default
	private List<String> languages = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "user_friends",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "friend_id"))
	@JsonIgnore
	@Builder.Default
	@ToString.Exclude
	private List<User> friends = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
			name = "user_favorite_genres",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "genre_id")
	)
	@Builder.Default
	private List<Genre> favoriteGenres = new ArrayList<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@Builder.Default
	private List<UUID> favoriteArtistsMbids = new ArrayList<>();

	@Column(name = "role")
	@Enumerated(value = EnumType.STRING)
	@JsonIgnore
	@Builder.Default
	private Role role = Role.USER;

	@Column(name = "is_online")
	@NotNull
	@Builder.Default
	private boolean online = false;

	@Column(name = "last_online")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonFormat(pattern = Constants.LOCAL_DATETIME_FORMAT)
	private LocalDateTime lastOnline;

	@Override
	@JsonIgnore
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(role.name()));
	}

	@Override
	public String getUsername() {
		return email;
	}
}
