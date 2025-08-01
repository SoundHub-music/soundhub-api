package com.soundhub.api.repositories;

import com.soundhub.api.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
	@Modifying
	@Query("delete Message m where m.id = ?1")
	void deleteById(UUID uuid);

	Page<Message> findByChat_Id(@Param("chatId") UUID chatId, Pageable pageable);

	List<Message> findAllByChat_Id(@Param("chatId") UUID chatId);
}
