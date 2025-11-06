package com.backend.geopacking.repository;

import com.backend.geopacking.model.Maquina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaquinaRepository extends JpaRepository<Maquina, Long> {
    Optional<Maquina> findByCodigo(String codigo);
    List<Maquina> findByActivo(boolean activo);
    @Query("SELECT m FROM Molino m WHERE m.activo = :activo")
    List<Maquina> findMolinosActivos(@Param("activo") boolean activo);
}
