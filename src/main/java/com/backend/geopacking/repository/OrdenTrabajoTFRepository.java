package com.backend.geopacking.repository;

import com.backend.geopacking.model.OrdenTrabajoTF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenTrabajoTFRepository extends JpaRepository<OrdenTrabajoTF, Long> {

    @Query("SELECT MAX(o.prioridad) FROM OrdenTrabajoTF o")
    Integer findMaxPrioridad();
}
