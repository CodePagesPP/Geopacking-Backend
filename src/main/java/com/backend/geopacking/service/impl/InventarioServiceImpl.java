package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.InventarioMovimientoDTO;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.InventarioMovimientoRepository;
import com.backend.geopacking.service.InventarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioMovimientoRepository inventarioRepository;

    @Override
    @Transactional
    public void registrarIngresoDesdeScrapp(Scrapp scrapp) {
        String codigo = scrapp.getNumeroBolson() + "/" + (scrapp.getAnio() % 100);

        InventarioMovimiento movimiento = InventarioMovimiento.builder()
                .codigoMovimiento(codigo)
                .tipo(TipoMovimiento.INGRESO)
                .origen(OrigenMovimiento.SCRAPP)
                .cantidad(scrapp.getPesoNeto()) // Usamos Peso Neto para inventario
                .fecha(scrapp.getFechaCreacion())
                .scrappReferencia(scrapp)
                .registradoPor(scrapp.getOperador())
                .build();

        inventarioRepository.save(movimiento);
    }

    @Override
    @Transactional
    public void registrarMovimientoManual(TipoMovimiento tipo, Double cantidad, User adminUser) {
        int anioActual = LocalDate.now().getYear();

        // 1. Obtener el último secuencial para este tipo y año
        Optional<InventarioMovimiento> ultimoMovimiento = inventarioRepository
                .findUltimoManualPorTipoAnio(tipo, anioActual);

        long nuevoSecuencial = 1;
        if (ultimoMovimiento.isPresent()) {
            String ultimoCodigo = ultimoMovimiento.get().getCodigoMovimiento();
            // Extraer el número del código. Ej: IN-MOLPP-5/2025 -> extraer 5
            // Formato esperado: PREFIJO-SEQ/AÑO
            try {
                String[] partes = ultimoCodigo.split("-"); // [IN, MOLPP, 5/2025]
                String parteSeqAnio = partes[2]; // 5/2025
                String seqStr = parteSeqAnio.split("/")[0]; // 5
                nuevoSecuencial = Long.parseLong(seqStr) + 1;
            } catch (Exception e) {
                // Fallback si el formato falla por alguna razón rara
                nuevoSecuencial = inventarioRepository.count() + 1;
            }
        }

        //Construir el nuevo código
        String prefijo = (tipo == TipoMovimiento.INGRESO) ? "IN" : "SA";
        String nuevoCodigo = String.format("%s-MOLPP-%d/%d", prefijo, nuevoSecuencial, anioActual);

        //Guardar
        InventarioMovimiento movimiento = InventarioMovimiento.builder()
                .codigoMovimiento(nuevoCodigo)
                .tipo(tipo)
                .origen(OrigenMovimiento.MANUAL)
                .cantidad(cantidad)
                .fecha(LocalDate.now())
                .registradoPor(adminUser)
                .build();

        inventarioRepository.save(movimiento);
    }

    @Override
    public Page<InventarioMovimientoDTO> listarMovimientos(LocalDate inicio, LocalDate fin, Pageable pageable) {
        Page<InventarioMovimiento> paginaEntidades;
        if (inicio != null && fin != null) {
            paginaEntidades = inventarioRepository.findAllByFechaBetween(inicio, fin, pageable);
        } else {
            paginaEntidades = inventarioRepository.findAll(pageable);
        }

        // Mapear de Entidad a DTO
        return paginaEntidades.map(this::mapToDTO);
    }

    private InventarioMovimientoDTO mapToDTO(InventarioMovimiento entidad) {
        InventarioMovimientoDTO dto = new InventarioMovimientoDTO();
        dto.setId(entidad.getId());
        dto.setCodigoMovimiento(entidad.getCodigoMovimiento());
        dto.setTipo(entidad.getTipo());
        dto.setOrigen(entidad.getOrigen());
        dto.setCantidad(entidad.getCantidad());
        dto.setFecha(entidad.getFecha());
        dto.setFechaRegistro(entidad.getFechaRegistro());

        // Aquí manejas la relación Lazy de forma segura
        if (entidad.getRegistradoPor() != null) {
            dto.setRegistradoPorNombre(entidad.getRegistradoPor().getName());
            // Asegúrate de que User tenga un campo 'name' o usa el que corresponda
        }

        return dto;
    }

    @Override
    public Double obtenerStockActual() {
        return inventarioRepository.calcularStockActual();
    }
}
