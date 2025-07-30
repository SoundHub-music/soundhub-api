package com.soundhub.api.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.soundhub.api.Constants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
public abstract class ContentEntity {
	@Id
	@GeneratedValue
	@UuidGenerator(style = UuidGenerator.Style.TIME)
	protected UUID id;

	@Column(name = "created_at")
	@JsonFormat(pattern = Constants.LOCAL_DATETIME_FORMAT)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	protected LocalDateTime createdAt;

	@ManyToOne
	@JoinColumn(name = "user_id")
	protected User author;

	@Column(name = "content", columnDefinition = "TEXT")
	protected String content;
}
