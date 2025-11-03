package com.backend.geopacking.repository;

import com.backend.geopacking.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    Optional<Reporte> findByDni(String dni);

    @Query("SELECT r FROM Reporte r WHERE r.role.name = :roleName")
    List<Reporte> findReporteByRoleName(@Param("roleName") String roleName);

    @Query("SELECT r FROM Reporte r WHERE r.id = :id AND r.role.name = 'REPORT'")
    Optional<Reporte> findReporteById(@Param("id") long id);
}
