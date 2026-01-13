package com.backend.geopacking.service;

import com.backend.geopacking.dto.OrdenTrabajoEXDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface OrdenTrabajoEXService {
    OrdenTrabajoEXDTO crearOrden(OrdenTrabajoEXDTO dto, String dniUsuario);
    List<OrdenTrabajoEXDTO> listarOrdenes();
    List<OrdenTrabajoEXDTO> listarOrdenesPrioridad();
    void actualizarPrioridades(List<OrdenTrabajoEXDTO> listaOrdenada);
    List<OrdenTrabajoEXDTO> listarOrdenesOT();
    OrdenTrabajoEXDTO editarOrden(Long id, OrdenTrabajoEXDTO dto);
    void eliminarOrden(Long id);
    Page<OrdenTrabajoEXDTO> listarPaginado(
            Long maquinaId,
            Long productoId,
            String estadoStr,
            LocalDate fDesde,
            LocalDate fHasta,
            Pageable pageable);
}