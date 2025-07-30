package com.soundhub.api.repositories;

import com.soundhub.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByEmail(String email);

	Boolean existsByEmail(String email);

	@Query("SELECT u FROM User u WHERE u.id IN :userIds")
	List<User> findByUserIds(List<UUID> userIds);

	@Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
	List<User> searchByFirstNameOrLastName(String firstName, String lastName);

	@Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) AND LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
	List<User> searchByFirstNameAndLastName(String firstName, String lastName);
}
