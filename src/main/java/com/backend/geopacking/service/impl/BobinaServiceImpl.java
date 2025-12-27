package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.BobinaTransitoDTO;
import com.backend.geopacking.model.BobinaEX;
import com.backend.geopacking.repository.BobinaEXRepository;
import com.backend.geopacking.service.BobinaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BobinaServiceImpl implements BobinaService {

    private final BobinaEXRepository bobinaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BobinaTransitoDTO> listarBobinasEnTransito() {
        return bobinaRepository.findAllWithDetails().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private BobinaTransitoDTO mapToDTO(BobinaEX b) {
        String prodNombre = "Desconocido";
        String otCodigo = "-";
        String operador = "Sin Asignar";
        LocalDateTime fecha = null;

        if (b.getTurno() != null) {
            fecha = b.getTurno().getFechaHoraFin();
            String nombreTurno = b.getTurno().getUsuarioNombre();

            if (nombreTurno != null && !nombreTurno.trim().equalsIgnoreCase("null null") && !nombreTurno.trim().isEmpty()) {
                operador = nombreTurno;
            } else {
                if (b.getTurno().getOrdenTrabajo() != null && b.getTurno().getOrdenTrabajo().getCreadaPor() != null) {
                    operador = b.getTurno().getOrdenTrabajo().getCreadaPor().getName();
                }
            }

            if (b.getTurno().getOrdenTrabajo() != null) {
                otCodigo = b.getTurno().getOrdenTrabajo().getCodigo();
                if (b.getTurno().getOrdenTrabajo().getProducto() != null) {
                    prodNombre = b.getTurno().getOrdenTrabajo().getProducto().getName();
                }
            }
        }

        return BobinaTransitoDTO.builder()
                .id(b.getId())
                .codigoBobina(b.getCodigo())
                .nombreProducto(prodNombre)
                .pesoBruto(b.getPesoBruto())
                .pesoNeto(b.getPesoNeto())
                .producidoPor(operador)
                .fecha(fecha)
                .codigoOT(otCodigo)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Double obtenerStockTotal() {
        return bobinaRepository.sumarPesoNetoTotal();
    }
}
