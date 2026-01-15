package com.backend.geopacking.repository;

import com.backend.geopacking.dto.BobinaHistorialDTO;
import com.backend.geopacking.model.BobinaEX;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BobinaEXRepository extends JpaRepository<BobinaEX, Long> {

    @Query("SELECT new com.backend.geopacking.dto.BobinaHistorialDTO(" +
            "b.id, " +
            "b.turno.fechaHoraFin, " +
            "b.codigo, " +
            "'PRODUCCION', " +
            "b.turno.ordenTrabajo.producto.code, " +
            "b.turno.usuarioNombre, " +
            "b.pesoBruto, " +
            "b.pesoNeto, " +
            "b.turno.ordenTrabajo.producto.name, " +
            "b.turno.ordenTrabajo.maquina.codigo, " +
            "b.turno.ordenTrabajo.codigo" +
            ") " +
            "FROM BobinaEX b " +
            "WHERE (cast(:inicio as timestamp) IS NULL OR b.turno.fechaHoraFin >= :inicio) " +
            "AND (cast(:fin as timestamp) IS NULL OR b.turno.fechaHoraFin <= :fin) " +
            "ORDER BY b.turno.fechaHoraFin DESC")
    Page<BobinaHistorialDTO> buscarHistorialPaginado(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            Pageable pageable
    );

    @Query("SELECT b FROM BobinaEX b " +
            "JOIN FETCH b.turno t " +
            "JOIN FETCH t.ordenTrabajo ot " +
            "JOIN FETCH ot.producto p " +
            "ORDER BY b.id DESC")
    List<BobinaEX> findAllWithDetails();

    @Query("SELECT COALESCE(SUM(b.pesoNeto), 0) FROM BobinaEX b")
    Double sumarPesoNetoTotal();

    @Query("SELECT new com.backend.geopacking.dto.BobinaHistorialDTO(" +
            "b.id, b.turno.fechaHoraFin, b.codigo, 'PRODUCCION', " +
            "b.turno.ordenTrabajo.producto.code, b.turno.usuarioNombre, " +
            "b.pesoBruto, b.pesoNeto, " +
            "b.turno.ordenTrabajo.producto.name, " +
            "b.turno.ordenTrabajo.maquina.codigo, " +
            "b.turno.ordenTrabajo.codigo) " +
            "FROM BobinaEX b WHERE b.id = :id")
    Optional<BobinaHistorialDTO> obtenerBobinaPorId(@Param("id") Long id);

    @Query("SELECT COUNT(b) FROM BobinaEX b WHERE b.turno.ordenTrabajo.id = :otId")
    Long contarBobinasPorOT(@Param("otId") Long otId);

    @Query("SELECT b FROM BobinaEX b " +
            "JOIN FETCH b.turno t " +
            "JOIN FETCH t.ordenTrabajo ot " +
            "JOIN FETCH ot.producto p " +
            "WHERE b.estado = 'DISPONIBLE' " +
            "ORDER BY b.id DESC")
    List<BobinaEX> findDisponiblesWithDetails();

    Optional<BobinaEX> findByCodigoIgnoreCase(String codigo);
}
