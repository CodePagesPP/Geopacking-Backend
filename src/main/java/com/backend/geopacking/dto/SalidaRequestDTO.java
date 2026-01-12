package com.backend.geopacking.dto;

import lombok.Data;

import java.util.List;

@Data
public class SalidaRequestDTO {
    private List<TfSalidaDTO> items;
}
