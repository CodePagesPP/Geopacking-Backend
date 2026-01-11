package com.backend.geopacking.dto;

import lombok.Data;

@Data
public class RegistroProduccionTFDTO {
    private Long otId;
    private String codigoBobina;
    private String loteBobina;
    private String nombreBobina;

    private Double velocidad;
    private String horaInicio;
    private String horaFin;

    private Integer cajas;
    private Double rechazoKg;

    private Double pesoPromedio;
    private Boolean bobinaFin;
}
