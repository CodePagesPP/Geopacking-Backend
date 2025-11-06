package com.backend.geopacking.dto;

import com.backend.geopacking.model.TipoMovimiento;
import lombok.Data;

@Data
public class InventarioManualDTO {
    private TipoMovimiento tipo;
    private Double cantidad;
}
