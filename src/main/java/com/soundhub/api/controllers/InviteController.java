package com.soundhub.api.controllers;

import com.soundhub.api.models.Invite;
import com.soundhub.api.models.User;
import com.soundhub.api.services.InviteService;
import com.soundhub.api.services.UserService;
import com.soundhub.api.util.mappers.UserMapper;
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

	@Autowired
	private UserMapper userMapper;

	@PostMapping("/create/{recipientId}")
	public ResponseEntity<Invite> createInvite(@PathVariable UUID recipientId) {
		User currentUser = userService.getCurrentUser();
		User recipient = userService.getUserById(recipientId);
		return new ResponseEntity<>(inviteService.createInvite(currentUser, recipient), HttpStatus.OK);
	}

	@PostMapping("/accept/{inviteId}")
	public ResponseEntity<Invite> acceptInvite(@PathVariable UUID inviteId) throws IOException {
		User currentUser = userService.getCurrentUser();
		return new ResponseEntity<>(inviteService.acceptInvite(currentUser, inviteId), HttpStatus.OK);
	}

	@PostMapping("/reject/{inviteId}")
	public ResponseEntity<Invite> rejectInvite(@PathVariable UUID inviteId) {
		User currentUser = userService.getCurrentUser();
		return new ResponseEntity<>(inviteService.rejectInvite(currentUser, inviteId), HttpStatus.OK);
	}

	@GetMapping
	public ResponseEntity<List<Invite>> getAllInvites() {
		User currentUser = userService.getCurrentUser();
		return new ResponseEntity<>(inviteService.getAllInvites(currentUser), HttpStatus.OK);
	}

	@DeleteMapping("/{inviteId}")
	public ResponseEntity<Invite> deleteInvite(@PathVariable UUID inviteId) {
		User currentUser = userService.getCurrentUser();
		return new ResponseEntity<>(inviteService.deleteInvite(currentUser, inviteId), HttpStatus.OK);
	}

	@GetMapping("/{senderId}/{recipientId}")
	public ResponseEntity<Invite> getInviteBySenderAndRecipient(@PathVariable UUID senderId, @PathVariable UUID recipientId) {
		return new ResponseEntity<>(inviteService.getInviteBySenderAndRecipient(senderId, recipientId), HttpStatus.OK);
	}
}
