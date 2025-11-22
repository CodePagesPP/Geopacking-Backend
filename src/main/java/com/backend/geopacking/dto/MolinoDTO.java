package com.backend.geopacking.dto;
import lombok.Data;

import java.util.Set;

@Data
public class MolinoDTO {
    private String codigo;
    private String nroSerie;
    private String marca;
    private String modelo;
    private boolean activo;
    private Set<Long> origenIds;
}