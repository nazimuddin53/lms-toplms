package com.toplms.master.users;

import com.toplms.domain.base.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    @Query("SELECT t FROM Role t WHERE t.name = :name")
    public Optional<Role> findByRoleName(@Param("name") String name);

}
