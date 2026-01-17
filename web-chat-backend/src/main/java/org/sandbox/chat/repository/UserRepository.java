package org.sandbox.chat.repository;

import org.sandbox.chat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByVkId(String vkId);
    Optional<User> findByUsername(String username);
}