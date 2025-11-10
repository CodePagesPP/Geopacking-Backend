package com.backend.geopacking.controller;

import com.backend.geopacking.model.ProductoEX;
import com.backend.geopacking.service.ProductoEXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prodEX")
public class ProductoEXController {

    private final ProductoEXService productoEXService;

    @Autowired
    public ProductoEXController(ProductoEXService productoEXService) { this.productoEXService = productoEXService; }

    @GetMapping
    public List<ProductoEX> getAll() { return productoEXService.getAllEx(); }

    @GetMapping("/{code}")
    public ResponseEntity<ProductoEX> getByCode(@PathVariable String code){
        return ResponseEntity.ok(productoEXService.getExByCode(code));
    }

    @GetMapping("/nombre/{name}")
    public ResponseEntity<ProductoEX> getByName(@PathVariable String name){
        return ResponseEntity.ok(productoEXService.getExByName(name));
    }

    @PostMapping()
    public ResponseEntity<ProductoEX> create(@RequestBody ProductoEX productoEX){
        ProductoEX Ex = productoEXService.createEX(productoEX);
            return new ResponseEntity<>(Ex, HttpStatus.CREATED);
    }

    @PutMapping("/{code}")
    public ResponseEntity<ProductoEX> update(@PathVariable String code, @RequestBody ProductoEX productoEX){
        ProductoEX ExUpdated = productoEXService.updateEX(productoEX, code);
        return ResponseEntity.ok(ExUpdated);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<ProductoEX> delete(@PathVariable String code){
        productoEXService.deleteEX(code);
        return ResponseEntity.noContent().build();
    }
}
