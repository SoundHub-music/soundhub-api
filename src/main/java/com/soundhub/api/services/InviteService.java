package com.soundhub.api.services;

import com.soundhub.api.models.Invite;
import com.soundhub.api.models.User;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface InviteService {
	Invite createInvite(User sender, User recipient);

	Invite acceptInvite(User inviteRecipient, UUID inviteId) throws IOException;

	Invite rejectInvite(User inviteRecipient, UUID inviteId);

	List<Invite> getAllInvites(User user);

	Invite deleteInvite(User user, UUID inviteId);

	Invite getInviteBySenderAndRecipient(UUID senderId, UUID recipientId);
}
