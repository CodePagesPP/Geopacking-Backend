package com.backend.geopacking.dto;

import com.backend.geopacking.model.Scrapp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedScrappReportDTO {
    private List<Scrapp> registros;
    private int currentPage;
    private long totalItems;
    private int totalPages;
    private double totalPesoBruto;
    private double totalPesoNeto;
}
