package com.backend.geopacking.service;

import com.backend.geopacking.dto.PaginatedScrappReportDTO;
import com.backend.geopacking.dto.ScrappDTO;
import com.backend.geopacking.dto.ScrappReportDTO;
import com.backend.geopacking.model.Scrapp;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.List;

public interface ScrappService {
    Scrapp registrarScrapp(ScrappDTO dto, UserDetails userDetails);

    List<Scrapp> obtenerTodos();

    Scrapp obtenerPorId(Long id);
    public byte[] generarReporteCompletoPdf(LocalDate fechaInicio, LocalDate fechaFin);
    public PaginatedScrappReportDTO obtenerReporteAdmin(Pageable pageable,
                                                        LocalDate fechaInicio,
                                                        LocalDate fechaFin,UserDetails userDetails);
}
