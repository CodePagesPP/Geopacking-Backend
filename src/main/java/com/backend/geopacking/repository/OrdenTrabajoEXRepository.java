package com.backend.geopacking.repository;

import com.backend.geopacking.model.EstadoOT_EX;
import com.backend.geopacking.model.OrdenTrabajoEX;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrdenTrabajoEXRepository extends JpaRepository<OrdenTrabajoEX, Long> {
    Optional<OrdenTrabajoEX> findByCodigo(String codigo);

    @Query("SELECT COALESCE(MAX(o.prioridad), 0) FROM OrdenTrabajoEX o")
    Integer findMaxPrioridad();

    @Query("SELECT o FROM OrdenTrabajoEX o " +
            "WHERE (:maquinaId IS NULL OR o.maquina.id = :maquinaId) " +
            "AND (:productoId IS NULL OR o.producto.id = :productoId) " +
            "AND (:estado IS NULL OR o.estado = :estado) " +
            "AND (cast(:fechaDesde as timestamp) IS NULL OR o.fechaCreacion >= :fechaDesde) " +
            "AND (cast(:fechaHasta as timestamp) IS NULL OR o.fechaCreacion <= :fechaHasta)")
    Page<OrdenTrabajoEX> filtrarOrdenes(
            @Param("maquinaId") Long maquinaId,
            @Param("productoId") Long productoId,
            @Param("estado") EstadoOT_EX estado,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta,
            Pageable pageable
    );
}
