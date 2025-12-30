package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.InsumoRegistroDTO;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.MaterialRepository;
import com.backend.geopacking.repository.MotivoRepository;
import com.backend.geopacking.repository.RegistroInsumoRepository;
import com.backend.geopacking.repository.UserRepository;
import com.backend.geopacking.service.InsumoService;
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
    public void registrarSalidaAutomatica(Long materialId, Double cantidad) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material no encontrado"));

        Motivo motivoProduccion = motivoRepository.findByNombreIgnoreCase("PRODUCCION")
                .orElseThrow(() -> new RuntimeException("Motivo 'PRODUCCION' no configurado en BD"));

        RegistroInsumo salida = RegistroInsumo.builder()
                .material(material)
                .cantidad(cantidad)
                .operacion(Operacion.SALIDA)
                .tipoRegistro(TipoRegistro.PRODUCCION)
                .fecha(LocalDate.now())
                .fechaRegistro(LocalDateTime.now())
                .motivo(motivoProduccion)
                .registradoPor(null)
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
}
