package com.backend.geopacking.controller;

import com.backend.geopacking.dto.ProductoDTO;
import com.backend.geopacking.model.ProductoTF;
import com.backend.geopacking.service.ProductoTFService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prodTF")
@AllArgsConstructor
public class ProductoTFController {

    private final ProductoTFService productoTFService;


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
    public ResponseEntity<ProductoTF> createTf(@Valid @RequestBody ProductoDTO dto){
        ProductoTF createdProductoTF = productoTFService.createTf(dto);
        return new  ResponseEntity<>(createdProductoTF, HttpStatus.CREATED);
    }

    @PutMapping("/{code}")
    public ResponseEntity<ProductoTF> updateTf(@PathVariable String code, @Valid @RequestBody ProductoDTO dto){
        ProductoTF updatedTF = productoTFService.updateTf(code, dto);
        return ResponseEntity.ok(updatedTF);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<ProductoTF> deleteTf(@PathVariable String code){
        productoTFService.deleteTf(code);
        return ResponseEntity.noContent().build();
    }
}
