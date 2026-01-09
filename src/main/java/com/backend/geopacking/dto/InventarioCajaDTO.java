package com.backend.geopacking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InventarioCajaDTO {
    private Long id;
    private String loteProduccion;
    private String nombreProducto;
    private Integer cantidad;
    private LocalDateTime fechaProduccion;
    private String estado;
}
