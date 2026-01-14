package com.backend.geopacking.dto;

import com.backend.geopacking.model.Operacion;
import com.backend.geopacking.model.TipoRegistro;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InsumoRegistroDTO {
    private Long id;
    private Double cantidad;
    private Operacion operacion;
    private LocalDate fecha;

    private Long materialId;
    private Long motivoId;
    private String nuevoMotivo;
    private String observaciones;

    private String materialNombre;
    private String motivoNombre;
    private String registradoPorNombre;
    private TipoRegistro tipoRegistro;
    private LocalDateTime fechaRegistro;
    private String codigoOT;
}