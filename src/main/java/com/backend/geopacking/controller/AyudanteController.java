package com.backend.geopacking.controller;

import com.backend.geopacking.dto.AyudanteDTO;
import com.backend.geopacking.dto.UserDTO;

import com.backend.geopacking.service.AyudanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class AyudanteController {
    private final AyudanteService ayudanteService;

    @PostMapping
    public ResponseEntity<UserDTO> registerOperador(@RequestBody AyudanteDTO AyudanteDTO) {
        return new ResponseEntity<>(ayudanteService.registerReporte(AyudanteDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllOperadores() {
        return ResponseEntity.ok(ayudanteService.getAllReportes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getOperadorById(@PathVariable long id) {
        return ResponseEntity.ok(ayudanteService.getReporteById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateOperador(@PathVariable long id, @RequestBody AyudanteDTO AyudanteDTO) {
        return ResponseEntity.ok(ayudanteService.updateReporte(id, AyudanteDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperador(@PathVariable long id) {
        ayudanteService.deleteReporte(id);
        return ResponseEntity.noContent().build();
    }
}
