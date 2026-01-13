package com.backend.geopacking.repository;

import com.backend.geopacking.model.InventarioCaja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventarioCajaRepository extends JpaRepository<InventarioCaja, Long> {
    List<InventarioCaja> findByEstado(String estado, Sort sort);
    @Query("SELECT i FROM InventarioCaja i " +
            "WHERE i.estado = 'EN_PT' " +
            "AND i.cantidad > 0 " +
            "AND i.detalleProduccion.ordenTrabajo.producto.code = :codigo " +
            "ORDER BY i.fechaProduccion ASC") // FIFO: Primero salen los más antiguos
    List<InventarioCaja> buscarPorCodigoProducto(@Param("codigo") String codigo);

    @Query("SELECT i FROM InventarioCaja i " +
            "WHERE i.estado = :estado " +
            "AND (cast(:fechaInicio as timestamp) IS NULL OR i.fechaProduccion >= :fechaInicio) " +
            "AND (cast(:fechaFin as timestamp) IS NULL OR i.fechaProduccion <= :fechaFin) " +
            "AND (:busqueda IS NULL OR " +
            "     LOWER(i.loteProduccion) LIKE :busqueda OR " +
            "     LOWER(i.nombreProducto) LIKE :busqueda)")
    Page<InventarioCaja> filtrarInventario(
            @Param("estado") String estado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("busqueda") String busqueda,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(i.cantidad), 0) FROM InventarioCaja i " +
            "WHERE i.estado = :estado " +
            "AND (cast(:fechaInicio as timestamp) IS NULL OR i.fechaProduccion >= :fechaInicio) " +
            "AND (cast(:fechaFin as timestamp) IS NULL OR i.fechaProduccion <= :fechaFin) " +
            "AND (:busqueda IS NULL OR " +
            "     LOWER(i.loteProduccion) LIKE :busqueda OR " +
            "     LOWER(i.nombreProducto) LIKE :busqueda)")
    Integer sumarStockTotal(
            @Param("estado") String estado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("busqueda") String busqueda
    );
}
