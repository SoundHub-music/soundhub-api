package com.soundhub.api.services.invite;

import com.soundhub.api.enums.InviteStatus;
import com.soundhub.api.exceptions.ApiException;
import com.soundhub.api.exceptions.ResourceNotFoundException;
import com.soundhub.api.models.Invite;
import com.soundhub.api.models.User;
import com.soundhub.api.repositories.InviteRepository;
import com.soundhub.api.services.strategies.invite.RejectInviteStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class RejectInviteStrategyTest {

	@Mock
	private InviteRepository inviteRepository;

	@InjectMocks
	private RejectInviteStrategy rejectInviteStrategy;

	private Invite validInvite;
	private User recipient;
	private UUID inviteId;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		recipient = User.builder()
				.id(UUID.randomUUID())
				.email("recipient@example.com")
				.firstName("John")
				.lastName("Doe")
				.build();

		inviteId = UUID.randomUUID();
		validInvite = Invite.builder()
				.id(inviteId)
				.sender(User.builder().id(UUID.randomUUID()).email("sender@example.com").build())
				.recipient(recipient)
				.status(InviteStatus.CONSIDERED)
				.build();
	}

	@Test
	void testExecute_Success() {
		when(inviteRepository.findById(inviteId)).thenReturn(Optional.of(validInvite));

		Invite result = rejectInviteStrategy.execute(recipient, inviteId);

		assertEquals(InviteStatus.REJECTED, result.getStatus(), "The invite status should be REJECTED.");
		verify(inviteRepository, times(1)).delete(validInvite);
	}

	@Test
	void testExecute_InviteNotFound() {
		when(inviteRepository.findById(inviteId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> rejectInviteStrategy.execute(recipient, inviteId),
				"ResourceNotFoundException should be thrown when invite is not found.");
	}

	@Test
	void testExecute_InvalidRecipient() {
		when(inviteRepository.findById(inviteId)).thenReturn(Optional.of(validInvite));

		User wrongRecipient = User.builder()
				.id(UUID.randomUUID())
				.email("wrongrecipient@example.com")
				.firstName("Jane")
				.lastName("Smith")
				.build();

		assertThrows(ApiException.class, () -> rejectInviteStrategy.execute(wrongRecipient, inviteId),
				"ApiException should be thrown when the recipient does not match.");
	}
}