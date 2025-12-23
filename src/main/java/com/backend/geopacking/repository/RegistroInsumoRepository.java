package com.backend.geopacking.repository;

import com.backend.geopacking.model.Material;
import com.backend.geopacking.model.Operacion;
import com.backend.geopacking.model.RegistroInsumo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RegistroInsumoRepository extends JpaRepository<RegistroInsumo, Long> {

    @Query("SELECT " +
            "COALESCE(SUM(CASE WHEN r.operacion = 'INGRESO' THEN r.cantidad ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN r.operacion = 'SALIDA' THEN r.cantidad ELSE 0 END), 0) " +
            "FROM RegistroInsumo r")
    Double calcularStockTotal();

    @Query("SELECT " +
            "COALESCE(SUM(CASE WHEN r.operacion = 'INGRESO' THEN r.cantidad ELSE 0 END), 0) - " +
            "COALESCE(SUM(CASE WHEN r.operacion = 'SALIDA' THEN r.cantidad ELSE 0 END), 0) " +
            "FROM RegistroInsumo r WHERE r.material.id = :materialId")
    Double calcularStockPorMaterial(@Param("materialId") Long materialId);

    @Query("SELECT r FROM RegistroInsumo r " +
            "WHERE r.tipoRegistro = 'MANUAL' " +
            "AND r.operacion = :operacion " +
            "AND r.material = :material " +
            "AND YEAR(r.fecha) = :anio " +
            "ORDER BY r.id DESC LIMIT 1")
    Optional<RegistroInsumo> findUltimoManualPorMaterialYAnio(
            @Param("operacion") Operacion operacion,
            @Param("material") Material material,
            @Param("anio") int anio);

    @Query("SELECT r FROM RegistroInsumo r " +
            "WHERE (:inicio IS NULL OR r.fecha >= :inicio) " +
            "AND (:fin IS NULL OR r.fecha <= :fin) " +
            "AND (:materialId IS NULL OR r.material.id = :materialId)")
    Page<RegistroInsumo> findWithFilters(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin,
            @Param("materialId") Long materialId,
            Pageable pageable);
}
