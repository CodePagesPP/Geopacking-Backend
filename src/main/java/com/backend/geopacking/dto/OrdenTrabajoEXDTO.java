package com.backend.geopacking.dto;

import com.backend.geopacking.model.EstadoOT_EX;
import com.backend.geopacking.model.Material;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrdenTrabajoEXDTO {

    private Long id;
    private String codigo;
    private LocalDateTime fechaCreacion;
    private Long maquinaId;
    private Long productoId;
    private Long creadaPorId;
    private String maquinaNombre;
    private String productoNombre;
    private String creadaPorUsername;
    private Double requerimientoKg;
    private Double producidoKg;
    private EstadoOT_EX estado;
    private Integer prioridad;
    private List<Material> materialesProducto;
}
