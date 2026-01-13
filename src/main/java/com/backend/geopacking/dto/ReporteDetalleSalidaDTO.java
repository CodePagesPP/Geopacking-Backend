package com.backend.geopacking.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReporteDetalleSalidaDTO {
    private String fecha;
    private String codigo;
    private String lote;
    private String cantidad;
}
