package com.electroverse.repository;

import com.electroverse.entity.FinderSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinderSessionRepository extends JpaRepository<FinderSession, Long> {

    List<FinderSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);
}
