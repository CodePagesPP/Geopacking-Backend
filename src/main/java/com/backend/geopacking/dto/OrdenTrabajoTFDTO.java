package com.backend.geopacking.dto;

import com.backend.geopacking.model.EstadoOT_TF;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrdenTrabajoTFDTO {
    private Long id;
    private String codigo;
    private LocalDateTime fechaCreacion;

    private Long maquinaId;
    private String maquinaNombre;

    private Long productoId;
    private String productoNombre;

    private String productoBaseNombre;

    private Double requerimientoKg;
    private Double producidoKg;

    private EstadoOT_TF estado;

    private String creadaPorUsername;
    private Integer prioridad;
}
