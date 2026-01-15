package com.backend.geopacking.repository;

import com.backend.geopacking.model.EstadoOT_TF;
import com.backend.geopacking.model.OrdenTrabajoTF;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrdenTrabajoTFRepository extends JpaRepository<OrdenTrabajoTF, Long> {

    @Query("SELECT MAX(o.prioridad) FROM OrdenTrabajoTF o")
    Integer findMaxPrioridad();

    @Query("SELECT o FROM OrdenTrabajoTF o " +
            "WHERE (:maquinaId IS NULL OR o.maquina.id = :maquinaId) " +
            "AND (:productoId IS NULL OR o.producto.id = :productoId) " +
            "AND (:estado IS NULL OR o.estado = :estado) " +
            "AND (cast(:fechaDesde as timestamp) IS NULL OR o.fechaCreacion >= :fechaDesde) " +
            "AND (cast(:fechaHasta as timestamp) IS NULL OR o.fechaCreacion <= :fechaHasta)")
    Page<OrdenTrabajoTF> filtrarOrdenes(
            @Param("maquinaId") Long maquinaId,
            @Param("productoId") Long productoId,
            @Param("estado") EstadoOT_TF estado, // Asegúrate de usar tu Enum correcto
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            Pageable pageable
    );
}
