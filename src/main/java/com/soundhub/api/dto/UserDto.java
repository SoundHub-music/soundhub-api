package com.soundhub.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.soundhub.api.Constants;
import com.soundhub.api.enums.Gender;
import com.soundhub.api.models.Genre;
import com.soundhub.api.models.TransformableUser;
import com.soundhub.api.models.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto implements TransformableUser {
	private UUID id;

	@NotBlank
	@Size(max = 254)
	@Email
	private String email;

	@NotBlank
	@Size(min = 8)
	private String password;

	@NotBlank
	@Size(min = 2, max = 255)
	private String firstName;

	@NotBlank
	@Size(min = 2, max = 255)
	private String lastName;

	@NotNull
	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
	private LocalDate birthday;

	private String city;
	private String country;
	private Gender gender;
	private String avatarUrl;
	private String description;
	private List<String> languages;
	private List<User> friends;
	private List<Genre> favoriteGenres;
	private List<UUID> favoriteArtistsMbids;

	@NotNull
	@Builder.Default
	private boolean online = false;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonFormat(pattern = Constants.LOCAL_DATETIME_FORMAT)
	private LocalDateTime lastOnline;
}
