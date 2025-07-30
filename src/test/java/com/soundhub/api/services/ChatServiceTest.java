package com.soundhub.api.services;

import com.soundhub.api.BaseTest;
import com.soundhub.api.dto.request.GroupChatRequest;
import com.soundhub.api.exceptions.ApiException;
import com.soundhub.api.exceptions.ResourceNotFoundException;
import com.soundhub.api.models.Chat;
import com.soundhub.api.models.User;
import com.soundhub.api.repositories.ChatRepository;
import com.soundhub.api.services.impl.ChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest extends BaseTest {

	@InjectMocks
	private ChatServiceImpl chatService;

	@Mock
	private ChatRepository chatRepository;

	@Mock
	private UserService userService;


	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		initUser();
		initChat();
	}

	@Test
	public void testCreateChat_Positive() throws ResourceNotFoundException {
		User recipient = User.builder().id(UUID.randomUUID()).build();

		when(userService.getUserById(any(UUID.class))).thenReturn(recipient);
		when(chatRepository.findSingleChatByUsers(any(User.class), any(User.class))).thenReturn(Optional.empty());
		when(chatRepository.save(any(Chat.class))).thenReturn(chat);

		Chat result = chatService.createChat(user, recipient.getId());

		assertEquals(chat, result);
		verify(chatRepository, times(1)).save(any(Chat.class));
	}

	@Test
	public void testCreateChat_Negative() throws ResourceNotFoundException {
		when(userService.getUserById(any(UUID.class))).thenThrow(new ResourceNotFoundException("User", "id", UUID.randomUUID()));

		assertThrows(ResourceNotFoundException.class, () -> chatService.createChat(user, UUID.randomUUID()));
	}

	@Test
	public void testGetChatById_Positive() throws ResourceNotFoundException {
		when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

		Chat result = chatService.getChatById(chatId);

		assertEquals(chat, result);
	}

	@Test
	public void testGetChatById_Negative() {
		when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> chatService.getChatById(chatId));
	}

	@Test
	public void testFindAllChatsByUserId_Positive() throws ResourceNotFoundException {
		when(userService.getUserById(userId)).thenReturn(user);
		when(chatRepository.findChatsByUserId(any(UUID.class))).thenReturn(List.of(chat));

		List<Chat> result = chatService.findAllChatsByUserId(userId);

		assertEquals(1, result.size());
		assertEquals(chat, result.get(0));
	}

	@Test
	public void testCreateGroup_Positive() {
		GroupChatRequest request = new GroupChatRequest();
		request.setGroupName("Test Group");
		request.setUserIds(List.of(userId));

		when(userService.getUserById(any(UUID.class))).thenReturn(user);
		when(chatRepository.save(any(Chat.class))).thenReturn(chat);

		Chat result = chatService.createGroup(request, user);

		assertEquals(chat, result);
		verify(chatRepository, times(1)).save(any(Chat.class));
	}

	@Test
	public void testAddUserToGroup_Positive() throws ResourceNotFoundException {
		when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.getUserById(any(UUID.class))).thenReturn(user);

		Chat result = chatService.addUserToGroup(chatId, userId);

		assertEquals(chat, result);
		verify(chatRepository, times(1)).save(any(Chat.class));
	}

	@Test
	public void testAddUserToGroup_Negative() {
		when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> chatService.addUserToGroup(chatId, userId));

		when(userService.getCurrentUser()).thenReturn(user);
		when(chatRepository.findById(anotherChatId)).thenReturn(Optional.of(anotherChat));
		assertThrows(ApiException.class, () -> chatService.addUserToGroup(anotherChatId, userId));
		verify(chatRepository, times(2)).findById(any(UUID.class));
	}

	@Test
	public void testRemoveFromGroup_Positive() throws ResourceNotFoundException {
		when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
		when(userService.getCurrentUser()).thenReturn(user);
		when(userService.getUserById(any(UUID.class))).thenReturn(user);

		Chat result = chatService.removeFromGroup(chatId, userId);

		assertEquals(chat, result);
		verify(chatRepository, times(1)).save(any(Chat.class));
	}

	@Test
	public void testRemoveFromGroup_Negative() {
		when(chatRepository.findById(chatId)).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> chatService.removeFromGroup(chatId, anotherUserId));

		when(userService.getCurrentUser()).thenReturn(user);
		when(chatRepository.findById(anotherChatId)).thenReturn(Optional.of(anotherChat));
		assertThrows(ApiException.class, () -> chatService.removeFromGroup(anotherChatId, anotherUserId));

		verify(chatRepository, times(2)).findById(any(UUID.class));
	}

	@Test
	public void testRenameGroup_Positive() throws ResourceNotFoundException {
		String newGroupName = "New Group Name";
		when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

		Chat result = chatService.renameGroup(chatId, newGroupName, user);

		assertEquals(newGroupName, result.getChatName());
		verify(chatRepository, times(1)).save(any(Chat.class));
	}

	@Test
	public void testDeleteChat_Positive() throws ResourceNotFoundException {
		when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
		when(userService.getCurrentUser()).thenReturn(user);

		UUID result = chatService.deleteChat(chatId);

		assertEquals(chatId, result);
		verify(chatRepository, times(1)).delete(any(Chat.class));
	}

	@Test
	public void testDeleteChat_Negative_NotOwner() throws ResourceNotFoundException {
		when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
		when(userService.getCurrentUser()).thenReturn(anotherUser);

		assertThrows(ApiException.class, () -> chatService.deleteChat(chatId));
	}

	@Test
	public void testDeleteChat_Negative_ChatNotFound() throws ResourceNotFoundException {
		when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> chatService.deleteChat(chatId));
	}

}
