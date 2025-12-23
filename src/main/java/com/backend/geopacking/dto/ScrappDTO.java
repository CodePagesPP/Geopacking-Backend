package com.backend.geopacking.dto;

import lombok.Data;

@Data
public class ScrappDTO {
    private Long maquinaId;
    private Double pesoBruto;
    private Double pesoNeto;
    private Long typeScrappId;
    private Long origenId;
}
