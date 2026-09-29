package com.toplms.master.tenant;

import com.toplms.domain.base.Tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String>{
    Optional<Tenant> findById(String id);

    @Query("SELECT t FROM Tenant t WHERE LOWER(t.subdomain) = LOWER(:subdomain)")
    Optional<Tenant> findBySubdomain(@Param("subdomain") String subdomain);

    // JOIN FETCH avoids N+1 lazy-loading the plan for every row on the superadmin tenant list.
    @Query("SELECT t FROM Tenant t JOIN FETCH t.plan")
    List<Tenant> findAllWithPlan();

    long countByPlan_Id(String planId);
}
