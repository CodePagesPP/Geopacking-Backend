package com.backend.geopacking.repository;

import com.backend.geopacking.model.InventarioCaja;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventarioCajaRepository extends JpaRepository<InventarioCaja, Long> {
    List<InventarioCaja> findByEstado(String estado, Sort sort);
}
