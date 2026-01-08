package com.backend.geopacking.repository;

import com.backend.geopacking.model.DetalleProduccionTF;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DetalleProduccionTFRepository extends JpaRepository<DetalleProduccionTF, Long> {
    List<DetalleProduccionTF> findByOrdenTrabajoId(Long otId);
    List<DetalleProduccionTF> findAll(Sort sort);
    List<DetalleProduccionTF> findByFechaRegistroBetween(LocalDate inicio, LocalDate fin, Sort sort);
}
