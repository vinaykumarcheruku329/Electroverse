package com.electroverse.repository;

import com.electroverse.entity.User;
import com.electroverse.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRole(Role role);

    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByRole(Role role);
}
