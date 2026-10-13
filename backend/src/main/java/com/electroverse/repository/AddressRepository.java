package com.electroverse.repository;

import com.electroverse.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Address> findByIdAndUserId(Long id, Long userId);

    Optional<Address> findFirstByUserIdAndDefaultAddressTrue(Long userId);

    @Modifying
    @Query("update Address a set a.defaultAddress = false where a.user.id = :userId")
    void clearDefaultForUser(Long userId);
}
