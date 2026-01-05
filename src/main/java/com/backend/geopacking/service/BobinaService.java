package com.backend.geopacking.service;

import com.backend.geopacking.dto.BobinaTransitoDTO;

import java.time.LocalDate;
import java.util.List;

public interface BobinaService {
    List<BobinaTransitoDTO> listarBobinasEnTransito();
    Double obtenerStockTotal();
    byte[] generarReporteCompletoBobinasPdf(LocalDate fechaInicio, LocalDate fechaFin);
}
