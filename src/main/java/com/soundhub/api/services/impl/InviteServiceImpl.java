package com.soundhub.api.services.impl;

import com.soundhub.api.Constants;
import com.soundhub.api.enums.InviteStatus;
import com.soundhub.api.enums.InviteStrategyName;
import com.soundhub.api.exceptions.ApiException;
import com.soundhub.api.exceptions.InviteAlreadySentException;
import com.soundhub.api.exceptions.ResourceNotFoundException;
import com.soundhub.api.models.Invite;
import com.soundhub.api.models.User;
import com.soundhub.api.repositories.InviteRepository;
import com.soundhub.api.services.InviteService;
import com.soundhub.api.services.UserService;
import com.soundhub.api.services.strategies.invite.InviteStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class InviteServiceImpl implements InviteService {
	private final InviteStrategyFactory strategyFactory;

	@Autowired
	private InviteRepository inviteRepository;

	@Autowired
	private UserService userService;

	@Autowired
	public InviteServiceImpl(InviteStrategyFactory strategyFactory) {
		this.strategyFactory = strategyFactory;
	}

	@Override
	public Invite createInvite(User sender, User recipient) {
		log.info("createInvite[1]: user {} send invitation to friends for {}", sender, recipient);

		if (sender.getFriends().contains(recipient)) {
			log.error("createInvite[1]: error: {}", Constants.USER_ALREADY_FRIEND);
			throw new ApiException(HttpStatus.BAD_REQUEST, Constants.USER_ALREADY_FRIEND);
		}

		if (sender.equals(recipient)) {
			log.error("createInvite[2]: {}", Constants.USER_CANNOT_ADD_HIMSELF);
			throw new ApiException(HttpStatus.BAD_REQUEST, Constants.USER_CANNOT_ADD_HIMSELF);
		}

		if (inviteRepository.existsBySenderAndRecipient(sender, recipient)) {
			log.error("createInvite[3]: error: {}", Constants.INVITE_ALREADY_SENT_MSG);
			throw new InviteAlreadySentException(Constants.INVITE_ALREADY_SENT_MSG);
		}

		Invite invite = Invite.builder()
				.createdDateTime(LocalDateTime.now())
				.sender(sender)
				.recipient(recipient)
				.status(InviteStatus.CONSIDERED)
				.build();
		inviteRepository.save(invite);
		log.info("createInvite[2]: invite was successful saved, id: {}", invite.getId());
		return invite;
	}

	@Override
	public Invite acceptInvite(User inviteRecipient, UUID inviteId) {
		return strategyFactory.getStrategy(InviteStrategyName.AcceptInviteStrategy.name()).execute(inviteRecipient, inviteId);
	}

	@Override
	public Invite rejectInvite(User inviteRecipient, UUID inviteId) {
		return strategyFactory.getStrategy(InviteStrategyName.RejectInviteStrategy.name()).execute(inviteRecipient, inviteId);
	}

	@Override
	public List<Invite> getAllInvites(User user) {
		log.info("getAllInvites[1]: getting user invites: {}", user);
		List<Invite> invites = inviteRepository.findAllByRecipient(user);
		log.info("getAllInvites[2]: invites: {}", invites);
		return invites;
	}

	@Override
	public Invite deleteInvite(User sender, UUID inviteId) {
		return strategyFactory.getStrategy(InviteStrategyName.DeleteInviteStrategy.name()).execute(sender, inviteId);
	}

	@Override
	public Invite getInviteBySenderAndRecipient(UUID senderId, UUID recipientId) {
		log.info("getInviteBySenderAndRecipient[1]: getting invite sender: {}, recipient: {}", senderId, recipientId);
		User sender = userService.getUserById(senderId);
		User recipient = userService.getUserById(recipientId);
		return inviteRepository.findInviteBySenderAndRecipient(sender, recipient)
				.orElseThrow(() -> new ResourceNotFoundException(Constants.INVITE_RESOURCE_NAME));
	}
}
