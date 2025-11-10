package com.backend.geopacking.controller;

import com.backend.geopacking.dto.InventarioManualDTO;
import com.backend.geopacking.dto.InventarioMovimientoDTO;
import com.backend.geopacking.dto.InventarioStockDTO;
import com.backend.geopacking.model.InventarioMovimiento;
import com.backend.geopacking.model.Motivo;
import com.backend.geopacking.model.User;
import com.backend.geopacking.repository.MotivoRepository;
import com.backend.geopacking.repository.UserRepository;
import com.backend.geopacking.service.InventarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;
    private final UserRepository userRepository;
    private final MotivoRepository motivoRepository;

    @PostMapping("/manual")
    public ResponseEntity<Void> registrarMovimientoManual(
            @RequestBody InventarioManualDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        User adminUser = userRepository.findByDni(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Usuario administrador no encontrado"));

        inventarioService.registrarMovimientoManual(dto, adminUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/historial")
    public ResponseEntity<Page<InventarioMovimientoDTO>> obtenerHistorial(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long typeScrappId) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaRegistro"));

        Page<InventarioMovimientoDTO> historial = inventarioService.listarMovimientos(fechaInicio, fechaFin, typeScrappId,pageable);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/stock")
    public ResponseEntity<InventarioStockDTO> obtenerStockActual() {
        Double stock = inventarioService.obtenerStockActual();
        return ResponseEntity.ok(new InventarioStockDTO(stock));
    }

    @GetMapping("/motivos")
    public ResponseEntity<List<Motivo>> listarMotivos() {
        return ResponseEntity.ok(motivoRepository.findAll());
    }
}
