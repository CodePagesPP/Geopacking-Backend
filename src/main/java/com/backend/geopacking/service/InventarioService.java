package com.backend.geopacking.service;

import com.backend.geopacking.dto.InventarioMovimientoDTO;
import com.backend.geopacking.model.InventarioMovimiento;
import com.backend.geopacking.model.Scrapp;
import com.backend.geopacking.model.Operacion;
import com.backend.geopacking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public interface InventarioService {
    void registrarIngresoDesdeScrapp(Scrapp scrapp);
    void registrarMovimientoManual(Long typeScrappId,Operacion operacion, Double cantidad, User adminUser);
    Page<InventarioMovimientoDTO> listarMovimientos(LocalDate inicio, LocalDate fin,Long typeScrappId, Pageable pageable);
    Double obtenerStockActual();

    private InventarioMovimientoDTO mapToDTO(InventarioMovimiento inventarioMovimiento) {
        return null;
    }
}
