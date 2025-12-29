package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.InventarioManualDTO;
import com.backend.geopacking.dto.InventarioMovimientoDTO;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.InventarioMovimientoRepository;
import com.backend.geopacking.repository.MotivoRepository;
import com.backend.geopacking.repository.TypeScrappRepository;
import com.backend.geopacking.service.InventarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioMovimientoRepository inventarioRepository;
    private final TypeScrappRepository typeScrappRepository;
    private final MotivoRepository motivoRepository;

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
    public void registrarMovimientoManual(InventarioManualDTO dto, User adminUser) {
        int anioActual = LocalDate.now().getYear();

        TypeScrapp typeScrapp = typeScrappRepository.findById(dto.getTypeScrappId())
                .orElseThrow(() -> new EntityNotFoundException("TypeScrapp no encontrado"));

        Motivo motivo = null;
        if (dto.getNuevoMotivo() != null && !dto.getNuevoMotivo().isBlank()) {
            String nombre = dto.getNuevoMotivo().trim();
            motivo = motivoRepository.findByNombreIgnoreCase(nombre)
                    .orElseGet(() -> motivoRepository.save(Motivo.builder().nombre(nombre).build()));
        } else if (dto.getMotivoId() != null) {
            motivo = motivoRepository.findById(dto.getMotivoId())
                    .orElseThrow(() -> new EntityNotFoundException("Motivo no encontrado"));
        }

        Optional<InventarioMovimiento> ultimo = inventarioRepository
                .findUltimoManualPorTipoAnioYTipoScrapp(dto.getOperacion(), typeScrapp, anioActual);
        long nuevoSecuencial = 1;
        if (ultimo.isPresent()) {
            try {
                String[] partes = ultimo.get().getCodigoMovimiento().split("-");
                String seqStr = partes[2].split("/")[0];
                nuevoSecuencial = Long.parseLong(seqStr) + 1;
            } catch (Exception e) {
                nuevoSecuencial = inventarioRepository.count() + 1;
            }
        }

        String prefijo = (dto.getOperacion() == Operacion.INGRESO) ? "IN" : "SA";
        String codigo = String.format("%s-MOLPP-%d/%d", prefijo, nuevoSecuencial, anioActual);

        InventarioMovimiento mov = InventarioMovimiento.builder()
                .codigoMovimiento(codigo)
                .operacion(dto.getOperacion())
                .tipoRegistro(TipoRegistro.MANUAL)
                .cantidad(dto.getCantidad())
                .fecha(LocalDate.now())
                .registradoPor(adminUser)
                .typeScrapp(typeScrapp)
                .motivo(motivo)
                .nota(dto.getNota())
                .build();

        inventarioRepository.save(mov);
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
        dto.setNota(entidad.getNota());

        if (entidad.getRegistradoPor() != null) {
            dto.setRegistradoPorNombre(entidad.getRegistradoPor().getName());
        }

        if (entidad.getTypeScrapp() != null) {
            dto.setTypeScrappNombre(entidad.getTypeScrapp().getName());
        }

        if (entidad.getMotivo() != null) {
            dto.setMotivoNombre(entidad.getMotivo().getNombre());
        }

        return dto;
    }

    @Override
    public void registrarSalidaAutomaticaScrapp(Long typeScrappId, Double cantidad) {
        TypeScrapp tipo = typeScrappRepository.findById(typeScrappId)
                .orElseThrow(() -> new RuntimeException("Tipo de Scrapp no encontrado"));

        Motivo motivoProd = motivoRepository.findByNombreIgnoreCase("PRODUCCION")
                .orElseThrow(() -> new RuntimeException("Motivo PRODUCCION no existe"));

        String codigoGen = "SAL-PROD-" + System.currentTimeMillis();

        InventarioMovimiento movimiento = InventarioMovimiento.builder()
                .codigoMovimiento(codigoGen)
                .operacion(Operacion.SALIDA)
                .tipoRegistro(TipoRegistro.PRODUCCION)
                .cantidad(cantidad)
                .fecha(LocalDate.now())
                .fechaRegistro(LocalDateTime.now())
                .typeScrapp(tipo)
                .motivo(motivoProd)
                .registradoPor(null)
                .nota("Consumo automático por Producción")
                .build();

        inventarioRepository.save(movimiento);
    }

    @Override
    public Double obtenerStockActual() {
        return inventarioRepository.calcularStockActual();
    }
}
