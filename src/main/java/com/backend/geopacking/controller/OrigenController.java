package com.backend.geopacking.controller;

import com.backend.geopacking.model.Origen;
import com.backend.geopacking.service.OrigenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/origins")
public class OrigenController {

    @Autowired
    private final OrigenService origenService;

    public OrigenController(OrigenService origenService) { this.origenService = origenService; }

    @GetMapping
    public List<Origen> getAllOrigins() { return origenService.getAllOrigins(); }

    @PostMapping
    public ResponseEntity<Origen> createOrigen(@RequestBody Origen origen) {
        Origen nuevoOrigen = origenService.createOrigin(origen);
        return new ResponseEntity<>(nuevoOrigen, HttpStatus.CREATED);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Origen> getOrigenByCode(@PathVariable String code) {
        return ResponseEntity.ok(origenService.getOriginByCode(code));
    }

    @GetMapping("/nombre/{name}")
    public ResponseEntity<Origen> getOrigenByName(@PathVariable String name) {
        return ResponseEntity.ok(origenService.getOriginByName(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Origen> updateOrigen(@PathVariable Long id, @RequestBody Origen origen) {
        Origen updatedOrigen = origenService.updateOrigin(id, origen);
        return ResponseEntity.ok(updatedOrigen);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrigen(@PathVariable Long id) {
        origenService.deleteOrigin(id);
        return ResponseEntity.noContent().build();
    }
}
