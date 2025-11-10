package com.backend.geopacking.repository;

import com.backend.geopacking.model.Motivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MotivoRepository extends JpaRepository<Motivo, Long> {
    Optional<Motivo> findByNombreIgnoreCase(String nombre);
}