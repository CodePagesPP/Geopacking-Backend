package com.backend.geopacking.repository;

import com.backend.geopacking.dto.BobinaHistorialDTO;
import com.backend.geopacking.model.BobinaEX;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BobinaEXRepository extends JpaRepository<BobinaEX, Long> {

    @Query("SELECT new com.backend.geopacking.dto.BobinaHistorialDTO(" +
            "t.fechaHoraFin, b.codigo, ot.codigo, p.code, t.usuarioNombre, b.pesoBruto, b.pesoNeto) " +
            "FROM BobinaEX b " +
            "JOIN b.turno t " +
            "JOIN t.ordenTrabajo ot " +
            "JOIN ot.producto p " +
            "ORDER BY t.fechaHoraFin DESC")
    List<BobinaHistorialDTO> obtenerHistorialCompleto();

    @Query("SELECT new com.backend.geopacking.dto.BobinaHistorialDTO(" +
            "t.fechaHoraFin, b.codigo, ot.codigo, p.code, t.usuarioNombre, b.pesoBruto, b.pesoNeto) " +
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
}
