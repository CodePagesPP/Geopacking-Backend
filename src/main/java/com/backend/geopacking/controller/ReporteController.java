package com.backend.geopacking.controller;

import com.backend.geopacking.dto.ReporteDTO;
import com.backend.geopacking.dto.UserDTO;

import com.backend.geopacking.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {
    private final ReporteService reporteService;

    @PostMapping
    public ResponseEntity<UserDTO> registerOperador(@RequestBody ReporteDTO ReporteDTO) {
        return new ResponseEntity<>(reporteService.registerReporte(ReporteDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllOperadores() {
        return ResponseEntity.ok(reporteService.getAllReportes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getOperadorById(@PathVariable long id) {
        return ResponseEntity.ok(reporteService.getReporteById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateOperador(@PathVariable long id, @RequestBody ReporteDTO ReporteDTO) {
        return ResponseEntity.ok(reporteService.updateReporte(id, ReporteDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperador(@PathVariable long id) {
        reporteService.deleteReporte(id);
        return ResponseEntity.noContent().build();
    }
}
