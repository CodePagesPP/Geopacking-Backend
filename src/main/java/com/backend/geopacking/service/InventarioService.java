package com.backend.geopacking.service;

import com.backend.geopacking.dto.InventarioManualDTO;
import com.backend.geopacking.dto.InventarioMovimientoDTO;
import com.backend.geopacking.model.InventarioMovimiento;
import com.backend.geopacking.model.Scrapp;
import com.backend.geopacking.model.User;
import com.itextpdf.text.DocumentException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface InventarioService {
    void registrarIngresoDesdeScrapp(Scrapp scrapp);
    void registrarMovimientoManual(InventarioManualDTO dto, User adminUser);
    Page<InventarioMovimientoDTO> listarMovimientos(LocalDate inicio, LocalDate fin,Long typeScrappId, Pageable pageable);
    Double obtenerStockActual();
    private InventarioMovimientoDTO mapToDTO(InventarioMovimiento inventarioMovimiento) {
        return null;
    }
    List<InventarioMovimientoDTO> listarMovimientosReporte(LocalDate inicio, LocalDate fin, Long typeScrappId);
    public byte[] generarPdfDisenoImagen(List<InventarioMovimientoDTO> lista, String rangoFechas, String filtroInfo) throws DocumentException;
}
