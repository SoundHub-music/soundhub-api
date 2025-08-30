package com.soundhub.api.controllers;

import com.soundhub.api.models.Invite;
import com.soundhub.api.models.User;
import com.soundhub.api.services.InviteService;
import com.soundhub.api.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/v1/invites")
public class InviteController {

	@Autowired
	private UserService userService;

	@Autowired
	private InviteService inviteService;

	@PostMapping("/create/{recipientId}")
	public ResponseEntity<Invite> createInvite(@PathVariable UUID recipientId) {
		User currentUser = userService.getCurrentUser();
		User recipient = userService.getUserById(recipientId);
		Invite invite = inviteService.createInvite(currentUser, recipient);

		return new ResponseEntity<>(invite, HttpStatus.OK);
	}

	@PostMapping("/accept/{inviteId}")
	public ResponseEntity<Invite> acceptInvite(@PathVariable UUID inviteId) throws IOException {
		User currentUser = userService.getCurrentUser();
		Invite acceptedInvite = inviteService.acceptInvite(currentUser, inviteId);

		return new ResponseEntity<>(acceptedInvite, HttpStatus.OK);
	}

	@PostMapping("/reject/{inviteId}")
	public ResponseEntity<Invite> rejectInvite(@PathVariable UUID inviteId) {
		User currentUser = userService.getCurrentUser();
		Invite rejectedInvite = inviteService.rejectInvite(currentUser, inviteId);

		return new ResponseEntity<>(rejectedInvite, HttpStatus.OK);
	}

	@GetMapping
	public ResponseEntity<List<Invite>> getAllInvites() {
		User currentUser = userService.getCurrentUser();
		List<Invite> invites = inviteService.getAllInvites(currentUser);

		return new ResponseEntity<>(invites, HttpStatus.OK);
	}

	@DeleteMapping("/{inviteId}")
	public ResponseEntity<Invite> deleteInvite(@PathVariable UUID inviteId) {
		User currentUser = userService.getCurrentUser();
		Invite deletedInvite = inviteService.deleteInvite(currentUser, inviteId);

		return new ResponseEntity<>(deletedInvite, HttpStatus.OK);
	}

	@GetMapping("/{senderId}/{recipientId}")
	public ResponseEntity<Invite> getInviteBySenderAndRecipient(
			@PathVariable UUID senderId,
			@PathVariable UUID recipientId
	) {
		Invite invite = inviteService.getInviteBySenderAndRecipient(senderId, recipientId);

		return new ResponseEntity<>(invite, HttpStatus.OK);
	}
}
