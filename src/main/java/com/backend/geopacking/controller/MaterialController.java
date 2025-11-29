package com.backend.geopacking.controller;

import com.backend.geopacking.model.Material;
import com.backend.geopacking.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/materials")
public class MaterialController {

    private final MaterialService materialService;

    @Autowired
    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    public List<Material> getAllMaterials() {
        return materialService.getAllMaterials();
    }

    @PostMapping
    public ResponseEntity<Material> createMaterial(@RequestBody Material material) {
        Material nuevoMaterial = materialService.createMaterial(material);
        return new ResponseEntity<>(nuevoMaterial, HttpStatus.CREATED);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Material> getMaterialByCode(@PathVariable String code) {
        return ResponseEntity.ok(materialService.getMaterialByCode(code));
    }

    @GetMapping("/nombre/{name}")
    public ResponseEntity<Material> getMaterialByName(@PathVariable String name) {
        return ResponseEntity.ok(materialService.getMaterialByName(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Material> updateMaterial(@PathVariable Long id, @RequestBody Material materialDetails) {
        Material updatedMaterial = materialService.updateMaterial(id, materialDetails);
        return ResponseEntity.ok(updatedMaterial);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }
}
