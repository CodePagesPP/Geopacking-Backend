package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.InventarioMovimientoDTO;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.InventarioMovimientoRepository;
import com.backend.geopacking.repository.TypeScrappRepository;
import com.backend.geopacking.service.InventarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioMovimientoRepository inventarioRepository;
    private final TypeScrappRepository typeScrappRepository;
    @Override
    @Transactional
    public void registrarIngresoDesdeScrapp(Scrapp scrapp) {
        String codigo = scrapp.getNumeroBolson() + "/" + (scrapp.getAnio() % 100);

        InventarioMovimiento movimiento = InventarioMovimiento.builder()
                .codigoMovimiento(codigo)
                .operacion(Operacion.INGRESO)
                .tipoRegistro(TipoRegistro.SCRAPP)
                .cantidad(scrapp.getPesoNeto()) // Usamos Peso Neto para inventario
                .fecha(scrapp.getFechaCreacion())
                .scrappReferencia(scrapp)
                .registradoPor(scrapp.getOperador())
                .typeScrapp(scrapp.getTypeScrapp())
                .build();

        inventarioRepository.save(movimiento);
    }

    @Override
    @Transactional
    public void registrarMovimientoManual(Long typeScrappId,Operacion tipo, Double cantidad, User adminUser) {
        int anioActual = LocalDate.now().getYear();

        TypeScrapp typeScrapp = typeScrappRepository.findById(typeScrappId)
                .orElseThrow(() -> new EntityNotFoundException("TypeScrapp no encontrado"));
        // 1. Obtener el último secuencial para este tipo y año
        Optional<InventarioMovimiento> ultimoMovimiento = inventarioRepository
                .findUltimoManualPorTipoAnioYTipoScrapp(tipo,typeScrapp, anioActual);

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
        String prefijo = (tipo == Operacion.INGRESO) ? "IN" : "SA";
        String nuevoCodigo = String.format("%s-MOLPP-%d/%d", prefijo, nuevoSecuencial, anioActual);

        //Guardar
        InventarioMovimiento movimiento = InventarioMovimiento.builder()
                .codigoMovimiento(nuevoCodigo)
                .operacion(tipo)
                .tipoRegistro(TipoRegistro.MANUAL)
                .cantidad(cantidad)
                .fecha(LocalDate.now())
                .registradoPor(adminUser)
                .typeScrapp(typeScrapp)
                .scrappReferencia(null)
                .build();

        inventarioRepository.save(movimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioMovimientoDTO> listarMovimientos(LocalDate inicio, LocalDate fin, Long typeScrappId,Pageable pageable) {
        Page<InventarioMovimiento> paginaEntidades = inventarioRepository.findWithFilters(
                inicio,
                fin,
                typeScrappId,
                pageable
        );

        // Mapear de Entidad a DTO
        return paginaEntidades.map(this::mapToDTO);
    }

    private InventarioMovimientoDTO mapToDTO(InventarioMovimiento entidad) {
        InventarioMovimientoDTO dto = new InventarioMovimientoDTO();
        dto.setId(entidad.getId());
        dto.setCodigoMovimiento(entidad.getCodigoMovimiento());
        dto.setOperacion(entidad.getOperacion());
        dto.setTipoRegistro(entidad.getTipoRegistro());
        dto.setCantidad(entidad.getCantidad());
        dto.setFecha(entidad.getFecha());
        dto.setFechaRegistro(entidad.getFechaRegistro());

        // Aquí manejas la relación Lazy de forma segura
        if (entidad.getRegistradoPor() != null) {
            dto.setRegistradoPorNombre(entidad.getRegistradoPor().getName());
            // Asegúrate de que User tenga un campo 'name' o usa el que corresponda
        }

        if (entidad.getTypeScrapp() != null) {
            // Asumo que TypeScrapp (que extiende MCO) tiene 'name'
            dto.setTypeScrappNombre(entidad.getTypeScrapp().getName());
        }

        return dto;
    }

    @Override
    public Double obtenerStockActual() {
        return inventarioRepository.calcularStockActual();
    }
}
