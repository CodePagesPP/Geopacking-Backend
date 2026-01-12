package com.backend.geopacking.service;

import com.backend.geopacking.dto.*;
import com.backend.geopacking.model.DetalleProduccionTF;
import com.backend.geopacking.model.InventarioCaja;
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
    void enviarAProductosTerminados(Long idInventario);
    List<InventarioCajaDTO> listarInventarioPorEstado(String estado);
    byte[] registrarSalidaMasiva(SalidaRequestDTO request, String username);
    List<InventarioCajaDTO> buscarInventarioPorCodigoProducto(String codigo);
}
