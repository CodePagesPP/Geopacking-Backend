package com.backend.geopacking.repository;

import com.backend.geopacking.dto.BobinaHistorialDTO;
import com.backend.geopacking.model.BobinaEX;
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
            "t.id, t.fechaHoraFin, b.codigo, ot.codigo, p.code, t.usuarioNombre, b.pesoBruto, b.pesoNeto) " +
            "FROM BobinaEX b " +
            "JOIN b.turno t " +
            "JOIN t.ordenTrabajo ot " +
            "JOIN ot.producto p " +
            "ORDER BY t.fechaHoraFin DESC")
    List<BobinaHistorialDTO> obtenerHistorialCompleto();

    @Query("SELECT new com.backend.geopacking.dto.BobinaHistorialDTO(" +
            "t.id, t.fechaHoraFin, b.codigo, ot.codigo, p.code, t.usuarioNombre, b.pesoBruto, b.pesoNeto) " +
            "FROM BobinaEX b " +
            "JOIN b.turno t " +
            "JOIN t.ordenTrabajo ot " +
            "JOIN ot.producto p " +
            "WHERE t.fechaHoraFin BETWEEN :inicio AND :fin " +
            "ORDER BY t.fechaHoraFin DESC")
    List<BobinaHistorialDTO> filtrarPorFechas(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
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
            "t.id, t.fechaHoraFin, b.codigo, ot.codigo, p.code, t.usuarioNombre, b.pesoBruto, b.pesoNeto) " +
            "FROM BobinaEX b " +
            "JOIN b.turno t " +
            "JOIN t.ordenTrabajo ot " +
            "JOIN ot.producto p " +
            "WHERE b.id = :id")
    Optional<BobinaHistorialDTO> obtenerBobinaPorId(@Param("id") Long id);

    @Query("SELECT COUNT(b) FROM BobinaEX b WHERE b.turno.ordenTrabajo.id = :otId")
    Long contarBobinasPorOT(@Param("otId") Long otId);
}
