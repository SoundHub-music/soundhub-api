package com.soundhub.api.services.strategies.invite;

import com.soundhub.api.models.Invite;
import com.soundhub.api.models.User;

import java.util.UUID;

/**
 * Strategy interface for handling different invite-related operations.
 * Implementations of this interface define specific behaviors
 * for processing invites in various scenarios, such as accepting, rejecting, or deleting an invite.
 */
public interface InviteStrategy {

	/**
	 * Executes the invite operation based on the specific strategy implementation.
	 *
	 * @param user     the user performing the operation. This could be the sender or recipient of the invite,
	 *                 depending on the context of the strategy.
	 * @param inviteId the unique identifier of the invite to be processed.
	 * @return the updated {@link Invite} object after the operation has been executed.
	 * @throws com.soundhub.api.exceptions.ResourceNotFoundException if the invite with the specified ID is not found.
	 * @throws com.soundhub.api.exceptions.ApiException              if the user does not have the required permissions
	 *                                                               to perform the operation or if any other error occurs.
	 */
	Invite execute(User user, UUID inviteId);
}
