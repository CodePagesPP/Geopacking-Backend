package com.backend.geopacking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class HistorialCajasDTO {
    private Long id;
    private Long otId;
    private LocalDate fecha;
    private LocalTime hora;
    private String otCodigo;
    private String producto;
    private String codigoBobina;
    private String loteBobina;
    private Integer cajas;
    private String operador;
    private Integer inicioSecuencia;
}
