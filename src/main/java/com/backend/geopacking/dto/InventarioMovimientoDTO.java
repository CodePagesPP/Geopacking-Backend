package com.backend.geopacking.dto;

import com.backend.geopacking.model.OrigenMovimiento;
import com.backend.geopacking.model.TipoMovimiento;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InventarioMovimientoDTO {
    private Long id;
    private String codigoMovimiento;
    private TipoMovimiento tipo;
    private OrigenMovimiento origen;
    private Double cantidad;
    private LocalDate fecha;
    private LocalDateTime fechaRegistro;
    private String registradoPorNombre;
}
