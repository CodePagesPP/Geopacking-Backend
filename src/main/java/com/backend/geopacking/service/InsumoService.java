package com.backend.geopacking.service;

import com.backend.geopacking.dto.InsumoRegistroDTO;
import com.backend.geopacking.model.RegistroInsumo;
import com.backend.geopacking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public interface InsumoService {
    InsumoRegistroDTO registrarInsumo(InsumoRegistroDTO dto, UserDetails userDetails);
    Page<InsumoRegistroDTO> listarInsumos(int page, int size, LocalDate inicio, LocalDate fin, Long materialId);
    Double obtenerStockMaterial(Long materialId);
    void registrarSalidaAutomatica(Long materialId, Double cantidad, Long otId);
}
