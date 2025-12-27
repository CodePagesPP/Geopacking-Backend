package com.backend.geopacking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BobinaTransitoDTO {
    private Long id;
    private String codigoBobina;
    private String nombreProducto;
    private Double pesoBruto;
    private Double pesoNeto;
    private String producidoPor;
    LocalDateTime fecha;
    private String codigoOT;
}
