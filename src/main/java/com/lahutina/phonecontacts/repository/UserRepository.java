package com.lahutina.phonecontacts.repository;


import com.lahutina.phonecontacts.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
