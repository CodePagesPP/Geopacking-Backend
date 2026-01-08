package com.backend.geopacking.service;

import com.backend.geopacking.dto.BobinaInfoDTO;
import com.backend.geopacking.dto.HistorialCajasDTO;
import com.backend.geopacking.dto.OrdenTrabajoTFDTO;
import com.backend.geopacking.dto.RegistroProduccionTFDTO;
import com.backend.geopacking.model.DetalleProduccionTF;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.List;

public interface OrdenTrabajoTFService {
    OrdenTrabajoTFDTO crearOrden(OrdenTrabajoTFDTO dto, String dniUsuario);
    List<OrdenTrabajoTFDTO> listarOrdenes();
    List<OrdenTrabajoTFDTO> listarOrdenesPrioridad();
    void actualizarPrioridades(List<OrdenTrabajoTFDTO> listaOrdenada);
    List<OrdenTrabajoTFDTO> listarOrdenesPendientes();
    void eliminarOrden(Long idOrden);
    OrdenTrabajoTFDTO actualizarOrden(Long idOrden, OrdenTrabajoTFDTO dto);
    List<DetalleProduccionTF> registrarAvance(List<RegistroProduccionTFDTO> dtos, String username);
    BobinaInfoDTO buscarBobinaPorCodigo(String codigo);
    List<HistorialCajasDTO> listarHistorial(LocalDate inicio, LocalDate fin);
}
