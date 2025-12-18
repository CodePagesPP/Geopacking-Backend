package com.backend.geopacking.repository;

import com.backend.geopacking.model.TurnoEX;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TurnoEXRepository extends JpaRepository<TurnoEX, Long> {
    List<TurnoEX> findByOrdenTrabajoIdOrderByIdDesc(Long otId);
}
