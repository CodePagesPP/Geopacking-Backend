package com.backend.geopacking.dto;

import com.backend.geopacking.model.TipoRegistro;
import com.backend.geopacking.model.Operacion;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InventarioMovimientoDTO {
    private Long id;
    private String codigoMovimiento;
    private Operacion operacion;
    private TipoRegistro tipoRegistro;
    private Double cantidad;
    private LocalDate fecha;
    private LocalDateTime fechaRegistro;
    private String TypeScrappNombre;
    private String registradoPorNombre;
    private String motivoNombre;
    private String nota;
    private String codigoOT;
}
