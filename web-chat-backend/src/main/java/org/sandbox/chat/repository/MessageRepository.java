package org.sandbox.chat.repository;

import org.sandbox.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m JOIN FETCH m.user ORDER BY m.createdAt DESC")
    List<Message> findLatestMessages(Pageable pageable);
    
    @Query("SELECT m FROM Message m JOIN FETCH m.user ORDER BY m.createdAt DESC")
    Page<Message> findLatestMessagesPaged(Pageable pageable);
}