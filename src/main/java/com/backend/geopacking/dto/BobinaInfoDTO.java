package com.backend.geopacking.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BobinaInfoDTO {
    private Long id;
    private String codigo;
    private String lote;
    private String nombreProducto;
    private Double pesoNeto;
}
