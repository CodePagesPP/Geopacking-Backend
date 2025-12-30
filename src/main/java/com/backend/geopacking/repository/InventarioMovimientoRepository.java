package com.backend.geopacking.repository;

import com.backend.geopacking.model.InventarioMovimiento;
import com.backend.geopacking.model.Operacion;
import com.backend.geopacking.model.TypeScrapp;
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


    @Query("SELECT " +
            "COALESCE(SUM(CASE WHEN m.operacion = 'INGRESO' THEN m.cantidad ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN m.operacion = 'SALIDA' THEN m.cantidad ELSE 0 END), 0) " +
            "FROM InventarioMovimiento m")
    Double calcularStockActual();


    @Query("SELECT m FROM InventarioMovimiento m " +
            "WHERE m.tipoRegistro = 'MANUAL' " +
            "AND m.operacion = :operacion " +
            "AND m.typeScrapp = :typeScrapp " +
            "AND YEAR(m.fecha) = :anio " +
            "ORDER BY m.id DESC LIMIT 1")
    Optional<InventarioMovimiento> findUltimoManualPorTipoAnioYTipoScrapp(
            @Param("operacion") Operacion operacion,
            @Param("typeScrapp") TypeScrapp typeScrapp,
            @Param("anio") int anio);

    @Query("SELECT m FROM InventarioMovimiento m " +
            "WHERE (CAST(:inicio AS LocalDate) IS NULL OR m.fecha >= :inicio) " +
            "AND (CAST(:fin AS LocalDate) IS NULL OR m.fecha <= :fin) " +
            "AND (CAST(:typeScrappId AS Long) IS NULL OR m.typeScrapp.id = :typeScrappId)")
    Page<InventarioMovimiento> findWithFilters(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin,
            @Param("typeScrappId") Long typeScrappId,
            Pageable pageable);
}
