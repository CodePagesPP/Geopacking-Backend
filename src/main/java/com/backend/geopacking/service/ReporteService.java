package com.backend.geopacking.service;

import com.backend.geopacking.dto.ReporteDTO;
import com.backend.geopacking.dto.UserDTO;

import java.util.List;

public interface ReporteService {
    UserDTO registerReporte(ReporteDTO reporte);
    List<UserDTO> getAllReportes();
    UserDTO getReporteById(long id);
    UserDTO updateReporte(long id, ReporteDTO Reporte);
    void deleteReporte(long id);
}
