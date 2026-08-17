package com.ams.repository;

import com.ams.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmailId(String emailId);
    boolean existsByUserName(String userName);
    boolean existsByEmailId(String emailId);
    boolean existsByPhone(String phone);
}
