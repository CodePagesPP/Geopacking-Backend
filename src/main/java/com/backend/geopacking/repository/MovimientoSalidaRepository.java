package com.backend.geopacking.repository;

import com.backend.geopacking.model.MovimientoSalida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface MovimientoSalidaRepository extends JpaRepository<MovimientoSalida, Long> {
    Page<MovimientoSalida> findByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin, Pageable pageable);
}
