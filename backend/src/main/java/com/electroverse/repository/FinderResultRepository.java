package com.electroverse.repository;

import com.electroverse.entity.FinderResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinderResultRepository extends JpaRepository<FinderResult, Long> {

    List<FinderResult> findBySessionIdOrderByRankPositionAsc(Long sessionId);
}
