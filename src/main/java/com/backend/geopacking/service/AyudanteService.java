package com.backend.geopacking.service;

import com.backend.geopacking.dto.AyudanteDTO;
import com.backend.geopacking.dto.UserDTO;

import java.util.List;

public interface AyudanteService {
    UserDTO registerReporte(AyudanteDTO reporte);
    List<UserDTO> getAllReportes();
    UserDTO getReporteById(long id);
    UserDTO updateReporte(long id, AyudanteDTO Reporte);
    void deleteReporte(long id);
}
