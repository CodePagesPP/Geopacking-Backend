package com.backend.geopacking.dto;

import com.backend.geopacking.model.Scrapp;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ScrappReportDTO {
    private List<Scrapp> registros;
    private double totalPesoBruto;
    private double totalPesoNeto;
}
