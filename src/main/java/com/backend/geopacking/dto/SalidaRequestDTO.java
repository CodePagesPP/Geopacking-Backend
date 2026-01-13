package com.backend.geopacking.dto;

import lombok.Data;

import java.util.List;

@Data
public class SalidaRequestDTO {
    private String motivo;
    private String comentarios;
    private List<TfSalidaDTO> items;
}
