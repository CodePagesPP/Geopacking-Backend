package com.backend.geopacking.repository;

import com.backend.geopacking.model.Ayudante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AyudanteRepository extends JpaRepository<Ayudante, Long> {

    Optional<Ayudante> findByDni(String dni);

    @Query("SELECT r FROM Ayudante r WHERE r.role.name = :roleName")
    List<Ayudante> findReporteByRoleName(@Param("roleName") String roleName);

    @Query("SELECT r FROM Ayudante r WHERE r.id = :id AND r.role.name = 'REPORT'")
    Optional<Ayudante> findReporteById(@Param("id") long id);
}
