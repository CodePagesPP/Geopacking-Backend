package com.backend.geopacking.service;

import com.backend.geopacking.dto.OrdenTrabajoEXDTO;

import java.util.List;

public interface OrdenTrabajoEXService {
    OrdenTrabajoEXDTO crearOrden(OrdenTrabajoEXDTO dto, String dniUsuario);
    List<OrdenTrabajoEXDTO> listarOrdenes();
    List<OrdenTrabajoEXDTO> listarOrdenesPrioridad();
    void actualizarPrioridades(List<OrdenTrabajoEXDTO> listaOrdenada);
    List<OrdenTrabajoEXDTO> listarOrdenesOT();
    OrdenTrabajoEXDTO editarOrden(Long id, OrdenTrabajoEXDTO dto);
    void eliminarOrden(Long id);
}