package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.InsumoRegistroDTO;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.*;
import com.backend.geopacking.service.InsumoService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InsumoServiceImpl implements InsumoService {

    private final RegistroInsumoRepository insumoRepository;
    private final MaterialRepository materialRepository;
    private final MotivoRepository motivoRepository;
    private final UserRepository userRepository;
    private final OrdenTrabajoEXRepository otExRepository;


    @Override
    @Transactional
    public InsumoRegistroDTO registrarInsumo(InsumoRegistroDTO dto, UserDetails userDetails) {
        User usuario = userRepository.findByDni(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Material material = materialRepository.findById(dto.getMaterialId())
                .orElseThrow(() -> new RuntimeException("Material no encontrado"));

        Motivo motivo = null;
        if (dto.getNuevoMotivo() != null && !dto.getNuevoMotivo().isBlank()) {
            String nombre = dto.getNuevoMotivo().trim();
            motivo = motivoRepository.findByNombreIgnoreCase(nombre)
                    .orElseGet(() -> motivoRepository.save(Motivo.builder().nombre(nombre).build()));
        } else if (dto.getMotivoId() != null) {
            motivo = motivoRepository.findById(dto.getMotivoId())
                    .orElseThrow(() -> new EntityNotFoundException("Motivo no encontrado"));
        }

        RegistroInsumo registro = RegistroInsumo.builder()
                .fecha(dto.getFecha())
                .operacion(dto.getOperacion())
                .cantidad(dto.getCantidad())
                .material(material)
                .motivo(motivo)
                .observaciones(dto.getObservaciones())
                .tipoRegistro(TipoRegistro.MANUAL)
                .registradoPor(usuario)
                .build();

        RegistroInsumo guardado = insumoRepository.save(registro);

        return mapToDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InsumoRegistroDTO> listarInsumos(int page, int size, LocalDate inicio, LocalDate fin, Long materialId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaRegistro"));

        Page<RegistroInsumo> pagina = insumoRepository.findWithFilters(inicio, fin, materialId, pageable);

        return pagina.map(this::mapToDTO);
    }

    @Override
    public Double obtenerStockMaterial(Long materialId) {
        return insumoRepository.calcularStockPorMaterial(materialId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarSalidaAutomatica(Long materialId, Double cantidad, Long otId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material no encontrado"));

        OrdenTrabajoEX ot = null;
        if(otId != null){
            ot = otExRepository.findById(otId).orElse(null);
        }

        Motivo motivoProduccion = motivoRepository.findByNombreIgnoreCase("PRODUCCION")
                .orElseThrow(() -> new RuntimeException("Motivo 'PRODUCCION' no configurado en BD"));

        User usuarioSistema = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Usuario Admin (ID 1) no encontrado para firmar la salida automática"));

        RegistroInsumo salida = RegistroInsumo.builder()
                .material(material)
                .cantidad(cantidad)
                .operacion(Operacion.SALIDA)
                .tipoRegistro(TipoRegistro.PRODUCCION)
                .fecha(LocalDate.now())
                .fechaRegistro(LocalDateTime.now())
                .motivo(motivoProduccion)
                .observaciones("Consumo automático generado por Producción")
                .registradoPor(usuarioSistema)
                .ordenTrabajo(ot)
                .build();

        insumoRepository.save(salida);
    }

    private InsumoRegistroDTO mapToDTO(RegistroInsumo entidad) {
        InsumoRegistroDTO dto = new InsumoRegistroDTO();
        dto.setId(entidad.getId());
        dto.setFecha(entidad.getFecha());
        dto.setFechaRegistro(entidad.getFechaRegistro());
        dto.setOperacion(entidad.getOperacion());
        dto.setCantidad(entidad.getCantidad());
        dto.setTipoRegistro(entidad.getTipoRegistro());
        dto.setObservaciones(entidad.getObservaciones());

        String otCodigo = "_";

        if (entidad.getOrdenTrabajo() != null) {
            otCodigo = entidad.getOrdenTrabajo().getCodigo();
        }

        dto.setCodigoOT(otCodigo);

        if (entidad.getMaterial() != null) {
            dto.setMaterialId(entidad.getMaterial().getId());
            dto.setMaterialNombre(entidad.getMaterial().getName());
        }

        if (entidad.getMotivo() != null) {
            dto.setMotivoId(entidad.getMotivo().getId());
            dto.setMotivoNombre(entidad.getMotivo().getNombre());
        }

        if (entidad.getRegistradoPor() != null) {
            dto.setRegistradoPorNombre(entidad.getRegistradoPor().getName());
        }

        return dto;
    }

    @PostConstruct
    public void init() {
        if (motivoRepository.findByNombreIgnoreCase("PRODUCCION").isEmpty()) {
            motivoRepository.save(Motivo.builder().nombre("PRODUCCION").build());
            System.out.println("MOTIVO SISTEMA 'PRODUCCION' CREADO AUTOMÁTICAMENTE");
        }
    }
}
