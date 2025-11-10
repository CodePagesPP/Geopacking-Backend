package com.backend.geopacking.dto;

import com.backend.geopacking.model.Operacion;
import lombok.Data;

@Data
public class InventarioManualDTO {
    private Operacion operacion;
    private Double cantidad;
    private Long motivoId;
    private String nuevoMotivo;
    private String nota;
    private Long typeScrappId;
}
