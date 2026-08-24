package com.toplms.master.users;

import com.toplms.domain.base.Role;
import com.toplms.domain.base.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    public Optional<User> findByEmail(@Param("email") String email);
}
