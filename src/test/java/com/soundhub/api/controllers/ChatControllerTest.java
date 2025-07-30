package com.soundhub.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soundhub.api.BaseTest;
import com.soundhub.api.Constants;
import com.soundhub.api.dto.request.GroupChatRequest;
import com.soundhub.api.dto.request.SingleChatRequest;
import com.soundhub.api.models.Chat;
import com.soundhub.api.models.User;
import com.soundhub.api.services.ChatService;
import com.soundhub.api.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class ChatControllerTest extends BaseTest {
	private final ObjectMapper objectMapper = new ObjectMapper();
	private MockMvc mockMvc;

	@Mock
	private ChatService chatService;

	@Mock
	private UserService userService;

	@InjectMocks
	private ChatController chatController;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
		initUser();
		initChat();
		generateAccessToken(user);
	}

	@Test
	void testCreateSingleChat_Positive() throws Exception {
		SingleChatRequest request = new SingleChatRequest();
		request.setRecipientId(UUID.randomUUID());

		// Мокаем сервисы
		when(userService.getCurrentUser()).thenReturn(user);
		when(chatService.createChat(any(User.class), any(UUID.class))).thenReturn(chat);

		mockMvc.perform(post("/api/v1/chats/single")
						.header("Authorization", bearerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists());
	}

	@Test
	void testCreateGroupChat_Positive() throws Exception {
		GroupChatRequest request = new GroupChatRequest();
		request.setGroupName("Test Group");
		request.setUserIds(Collections.singletonList(UUID.randomUUID()));

		when(userService.getCurrentUser()).thenReturn(user);
		when(chatService.createGroup(any(GroupChatRequest.class), any(User.class))).thenReturn(chat);

		mockMvc.perform(post("/api/v1/chats/group")
						.header("Authorization", bearerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists());
	}

	@Test
	void testFindChatById_Positive() throws Exception {
		when(chatService.getChatById(chatId)).thenReturn(chat);

		mockMvc.perform(get("/api/v1/chats/{chatId}", chatId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(chatId.toString()));
	}

	@Test
	void testFindChatsByUserId_Positive() throws Exception {
		List<Chat> chatList = Collections.singletonList(chat);

		when(chatService.findAllChatsByUserId(userId)).thenReturn(chatList);

		mockMvc.perform(get("/api/v1/chats/user/{userId}", userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void testAddUserToGroup_Positive() throws Exception {
		when(chatService.addUserToGroup(chatId, userId)).thenReturn(chat);

		mockMvc.perform(put("/api/v1/chats/{chatId}/add/{userId}", chatId, userId)
						.header("Authorization", bearerToken)
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists());
	}

	@Test
	void testRemoveUserFromGroup_Positive() throws Exception {
		when(chatService.removeFromGroup(chatId, userId)).thenReturn(chat);

		MvcResult result = mockMvc.perform(put("/api/v1/chats/{chatId}/remove/{userId}", chatId, userId)
						.header("Authorization", bearerToken)
				)
				.andExpect(status().isOk())
				.andReturn();

		log.debug("testRemoveUserFromGroup_Positive[1]: {}", result.getResponse().getContentAsString());
	}

	@Test
	void testDeleteChat_Positive() throws Exception {
		when(chatService.deleteChat(chatId)).thenReturn(chatId);

		mockMvc.perform(delete("/api/v1/chats/delete/{chatId}", chatId)
						.header("Authorization", bearerToken)
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value(String.format(Constants.CHAT_DELETE_SUCCESS_MSG, chatId)));
	}

	@Test
	void testRenameGroup_Positive() throws Exception {
		String newName = "New Group Name";

		when(userService.getCurrentUser()).thenReturn(new User());
		when(chatService.renameGroup(chatId, newName, userService.getCurrentUser())).thenReturn(chat);

		mockMvc.perform(put("/api/v1/chats/{chatId}/rename/{newName}", chatId, newName)
						.header("Authorization", bearerToken)
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists());
	}
}
