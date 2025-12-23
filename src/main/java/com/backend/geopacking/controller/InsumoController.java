package com.backend.geopacking.controller;

import com.backend.geopacking.dto.InsumoRegistroDTO;
import com.backend.geopacking.model.User;
import com.backend.geopacking.service.InsumoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/insumos")
@RequiredArgsConstructor
public class InsumoController {

    private final InsumoService insumoService;

    @PostMapping
    public ResponseEntity<InsumoRegistroDTO> registrar(@RequestBody InsumoRegistroDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        InsumoRegistroDTO resultado = insumoService.registrarInsumo(dto, userDetails);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping
    public ResponseEntity<Page<InsumoRegistroDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) LocalDate inicio,
            @RequestParam(required = false) LocalDate fin,
            @RequestParam(required = false) Long materialId
    ) {
        return ResponseEntity.ok(insumoService.listarInsumos(page, size, inicio, fin, materialId));
    }

    @GetMapping("/stock/{materialId}")
    public ResponseEntity<Double> obtenerStock(@PathVariable Long materialId) {
        return ResponseEntity.ok(insumoService.obtenerStockMaterial(materialId));
    }
}
