package com.backend.geopacking.repository;

import com.backend.geopacking.model.OrdenTrabajoEX;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrdenTrabajoEXRepository extends JpaRepository<OrdenTrabajoEX, Long> {
    Optional<OrdenTrabajoEX> findByCodigo(String codigo);
}
