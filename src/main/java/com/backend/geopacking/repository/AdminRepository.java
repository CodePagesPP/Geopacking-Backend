package com.backend.geopacking.repository;


import com.backend.geopacking.model.Admin;
import com.backend.geopacking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin,Long> {

    Optional<User> findByDni(String dni);
    @Query("SELECT c FROM Admin c WHERE c.role.name = :roleName")
    List<Admin> findAdminByRoleName(@Param("roleName") String role);
    @Query("SELECT c FROM Admin c WHERE c.id = :id AND c.role.name = 'ADMIN'")
    Optional<Admin> findAdminById(@Param("id") long id);
}
