package com.soundhub.api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "messages")
@SuperBuilder
public class Message extends ContentEntity {
	@Column(name = "isRead")
	private Boolean isRead;

	@Column(name = "reply_to_message_id")
	private UUID replyToMessageId;

	@ManyToOne
	@JoinColumn(name = "chat_id", referencedColumnName = "id")
	@JsonIgnore
	private Chat chat;

	@JsonProperty("chat_id")
	public UUID getChatId() {
		return chat.getId();
	}
}
