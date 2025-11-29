package com.backend.geopacking.controller;
import com.backend.geopacking.model.TypeScrapp;
import com.backend.geopacking.service.TypeScrappService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/typescrapp")
@AllArgsConstructor
public class TypeScrappController {
    @Autowired
    private final TypeScrappService typeScrappService;

    @GetMapping
    public List<TypeScrapp> getAllOrigins() { return typeScrappService.getAllTypeScrapps(); }

    @PostMapping
    public ResponseEntity<TypeScrapp> createtypeScrapp(@RequestBody TypeScrapp typeScrapp) {
        TypeScrapp nuevotypeScrapp = typeScrappService.createTypeScrapps(typeScrapp);
        return new ResponseEntity<>(nuevotypeScrapp, HttpStatus.CREATED);
    }

    @GetMapping("/{code}")
    public ResponseEntity<TypeScrapp> gettypeScrappByCode(@PathVariable String code) {
        return ResponseEntity.ok(typeScrappService.getTypeScrappsByCode(code));
    }

    @GetMapping("/nombre/{name}")
    public ResponseEntity<TypeScrapp> gettypeScrappByName(@PathVariable String name) {
        return ResponseEntity.ok(typeScrappService.getTypeScrappsByName(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TypeScrapp> updatetypeScrapp(@PathVariable Long id, @RequestBody TypeScrapp typeScrapp) {
        TypeScrapp updatedtypeScrapp = typeScrappService.updateTypeScrapps(id, typeScrapp);
        return ResponseEntity.ok(updatedtypeScrapp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletetypeScrapp(@PathVariable Long id) {
        typeScrappService.deleteTypeScrapps(id);
        return ResponseEntity.noContent().build();
    }
}
