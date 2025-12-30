package com.backend.geopacking.service;

import com.backend.geopacking.dto.OrdenTrabajoTFDTO;

import java.util.List;

public interface OrdenTrabajoTFService {
    OrdenTrabajoTFDTO crearOrden(OrdenTrabajoTFDTO dto, String dniUsuario);
    List<OrdenTrabajoTFDTO> listarOrdenes();
    List<OrdenTrabajoTFDTO> listarOrdenesPrioridad();
    void actualizarPrioridades(List<OrdenTrabajoTFDTO> listaOrdenada);
    List<OrdenTrabajoTFDTO> listarOrdenesPendientes();
}
