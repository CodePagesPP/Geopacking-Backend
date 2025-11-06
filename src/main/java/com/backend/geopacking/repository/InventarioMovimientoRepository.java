package com.backend.geopacking.repository;

import com.backend.geopacking.model.InventarioMovimiento;
import com.backend.geopacking.model.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Long> {
    Page<InventarioMovimiento> findAllByFechaBetween(LocalDate inicio, LocalDate fin, Pageable pageable);

    // Para calcular el stock actual: Suma de INGRESOS - Suma de SALIDAS
    @Query("SELECT " +
            "COALESCE(SUM(CASE WHEN m.tipo = 'INGRESO' THEN m.cantidad ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN m.tipo = 'SALIDA' THEN m.cantidad ELSE 0 END), 0) " +
            "FROM InventarioMovimiento m")
    Double calcularStockActual();

    // Esto es CLAVE para generar el siguiente código IN-MOLPP-#/AÑO
    @Query("SELECT m FROM InventarioMovimiento m " +
            "WHERE m.origen = 'MANUAL' AND m.tipo = :tipo AND YEAR(m.fecha) = :anio " +
            "ORDER BY m.id DESC LIMIT 1")
    Optional<InventarioMovimiento> findUltimoManualPorTipoAnio(@Param("tipo") TipoMovimiento tipo, @Param("anio") int anio);
}
