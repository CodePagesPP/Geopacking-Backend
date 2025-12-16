package com.backend.geopacking.service;

import com.backend.geopacking.dto.OrdenTrabajoEXDTO;

import java.util.List;

public interface OrdenTrabajoEXService {
    OrdenTrabajoEXDTO crearOrden(OrdenTrabajoEXDTO dto, String dniUsuario);
    List<OrdenTrabajoEXDTO> listarOrdenes();
}