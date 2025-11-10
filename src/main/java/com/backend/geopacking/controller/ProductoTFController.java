package com.backend.geopacking.controller;

import com.backend.geopacking.model.ProductoTF;
import com.backend.geopacking.service.ProductoTFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prodTF")
public class ProductoTFController {

    private final ProductoTFService productoTFService;

    @Autowired
    public ProductoTFController(ProductoTFService productoTFService) { this.productoTFService = productoTFService; }

    @GetMapping
    public List<ProductoTF> getAll() { return productoTFService.getAllTf(); }

    @GetMapping("/{code}")
    public ResponseEntity<ProductoTF> getByCode(@PathVariable String code){
        return ResponseEntity.ok(productoTFService.getTfByCode(code));
    }

    @GetMapping("/nombre/{name}")
    public ResponseEntity<ProductoTF> getByName(@PathVariable String name){
        return ResponseEntity.ok(productoTFService.getTfByName(name));
    }

    @PostMapping
    public ResponseEntity<ProductoTF> createTf(@RequestBody ProductoTF productoTF){
        ProductoTF createdProductoTF = productoTFService.createTf(productoTF);
        return new  ResponseEntity<>(createdProductoTF, HttpStatus.CREATED);
    }

    @PutMapping("/{code}")
    public ResponseEntity<ProductoTF> updateTf(@PathVariable String code, @RequestBody ProductoTF productoTF){
        ProductoTF updatedTF = productoTFService.updateTf(code, productoTF);
        return ResponseEntity.ok(updatedTF);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<ProductoTF> deleteTf(@PathVariable String code){
        productoTFService.deleteTf(code);
        return ResponseEntity.noContent().build();
    }
}
