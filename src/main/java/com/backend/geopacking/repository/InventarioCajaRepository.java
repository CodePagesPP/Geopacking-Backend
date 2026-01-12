package com.backend.geopacking.repository;

import com.backend.geopacking.model.InventarioCaja;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
