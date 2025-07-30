package com.soundhub.api.services.impl;

import com.soundhub.api.Constants;
import com.soundhub.api.dto.request.SendMessageRequest;
import com.soundhub.api.dto.response.UnreadMessagesResponse;
import com.soundhub.api.exceptions.ApiException;
import com.soundhub.api.exceptions.ResourceNotFoundException;
import com.soundhub.api.models.Chat;
import com.soundhub.api.models.Message;
import com.soundhub.api.models.User;
import com.soundhub.api.repositories.MessageRepository;
import com.soundhub.api.services.ChatService;
import com.soundhub.api.services.MessageService;
import com.soundhub.api.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MessageServiceImpl implements MessageService {
	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private ChatService chatService;

	@Override
	public Message sendMessage(SendMessageRequest request) {
		User user = userService.getUserById(request.getUserId());
		Chat chat = chatService.getChatById(request.getChatId());

		Message message = Message.builder()
				.chat(chat)
				.author(user)
				.content(request.getContent())
				.replyToMessageId(request.getReplyToMessageId())
				.createdAt(LocalDateTime.now())
				.isRead(false)
				.build();

		Message savedMessage = messageRepository.save(message);

		List<Message> messages = findAllMessagesByChatId(chat.getId());
		chatService.updateMessageCount(chat.getId(), messages.size());

		return savedMessage;
	}

	@Override
	public Page<Message> findPagedMessagesByChatId(
			UUID chatId,
			User reqUser,
			int page,
			int size,
			String sort,
			String order
	) {
		Chat chat = chatService.getChatById(chatId);
		int adjustedPage = (page > 0) ? page - 1 : 0;

		Sort sortType = order.equalsIgnoreCase("asc")
				? Sort.by(sort).ascending()
				: Sort.by(sort).descending();

		PageRequest pageRequest = PageRequest.of(adjustedPage, size, sortType);
		Page<Message> pages = messageRepository.findByChat_Id(chatId, pageRequest);

		if (adjustedPage > pages.getTotalPages())
			throw new ApiException(
					HttpStatus.NOT_FOUND,
					String.format(Constants.MESSAGE_PAGE_NOT_FOUND, pages.getTotalPages())
			);

		if (!chat.getParticipants().contains(reqUser)) {
			throw new ApiException(HttpStatus.FORBIDDEN, Constants.CHAT_NOT_CONTAINS_USER);
		}

		return pages;
	}

	@Override
	public List<Message> findAllMessagesByChatId(UUID chatId) {
		return messageRepository.findAllByChat_Id(chatId)
				.stream()
				.toList();
	}

	@Override
	public UnreadMessagesResponse getUnreadMessages() {
		UUID currentUserId = userService.getCurrentUser().getId();
		List<Chat> chats = chatService.findAllChatsByUserId(currentUserId);
		List<Message> unreadMessages = new ArrayList<>();

		chats.forEach(chat -> {
			List<Message> chatMessages = findAllMessagesByChatId(chat.getId())
					.stream()
					.filter(msg -> msg.getAuthor().getId() != currentUserId && !msg.getIsRead())
					.toList();

			unreadMessages.addAll(chatMessages);
		});

		return UnreadMessagesResponse.builder()
				.messages(unreadMessages)
				.count(unreadMessages.size())
				.build();
	}

	@Override
	public Message findMessageById(UUID messageId) {
		return messageRepository.findById(messageId)
				.orElseThrow(() -> new ResourceNotFoundException(Constants.MESSAGE_RESOURCE_NAME, Constants.ID_FIELD, messageId));
	}

	@Override
	@Transactional
	public UUID deleteMessageById(UUID messageId, User reqUser) {
		Message message = findMessageById(messageId);

		if (message.getAuthor().getId().equals(reqUser.getId())) {
			messageRepository.deleteById(message.getId());
		} else {
			throw new ApiException(HttpStatus.FORBIDDEN, Constants.PERMISSION_MESSAGE);
		}
		return message.getId();
	}

	@Override
	public Message changeMessage(UUID messageId, String newContent, User reqUser) {
		Message message = findMessageById(messageId);

		if (message.getAuthor().getId().equals(reqUser.getId())) {
			message.setContent(newContent);
			messageRepository.save(message);
		} else {
			throw new ApiException(HttpStatus.FORBIDDEN, Constants.PERMISSION_MESSAGE);
		}
		return message;
	}

	@Override
	public Message markMessageAsRead(UUID messageId) {
		Message message = findMessageById(messageId);
		message.setIsRead(true);
		messageRepository.save(message);

		return message;
	}
}
