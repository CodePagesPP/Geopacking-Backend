package com.backend.geopacking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BobinaHistorialDTO {
    private LocalDateTime fecha;
    private String codigoBobina;
    private String operacion;
    private String codigoProducto;
    private String operador;
    private Double pesoBruto;
    private Double pesoNeto;
}
