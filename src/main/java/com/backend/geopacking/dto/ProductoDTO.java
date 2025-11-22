package com.backend.geopacking.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductoDTO {
    private String name;
    private String code;
    private String referencia;
    private String marca;
    private String linea;
    private String categoria;
    private String unidadDeMedida;
    private Double pesoUnitario;
    private boolean activo;
    private Long materialId;
    private List<Long> materialesIds;
    private Long colorId;
}
