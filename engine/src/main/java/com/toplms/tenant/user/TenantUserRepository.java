package com.toplms.tenant.user;

import com.toplms.domain.tenant.TenantUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantUserRepository extends JpaRepository<TenantUser, String> {

    @Query("SELECT u FROM TenantUser u WHERE LOWER(u.email) = LOWER(:email) AND u.tenant.id = :tenantId")
    public Optional<TenantUser> findByEmail(@Param("email") String email, @Param("tenantId") String tenantId);

    long countByTenant_Id(String tenantId);

    @Query("SELECT u.tenant.id AS tenantId, COUNT(u) AS userCount FROM TenantUser u GROUP BY u.tenant.id")
    List<TenantUserCountProjection> countUsersGroupedByTenant();
}
