package com.backend.geopacking.controller;

import com.backend.geopacking.dto.ProductoDTO;
import com.backend.geopacking.model.ProductoEX;
import com.backend.geopacking.service.ProductoEXService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prodEX")
@AllArgsConstructor
public class ProductoEXController {

    private final ProductoEXService productoEXService;
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
    public ResponseEntity<ProductoEX> create(@Valid @RequestBody ProductoDTO dto){
        ProductoEX ex = productoEXService.createEX(dto);
        return new ResponseEntity<>(ex, HttpStatus.CREATED);
    }

    @PutMapping("/{code}")
    public ResponseEntity<ProductoEX> update(@PathVariable String code, @Valid @RequestBody ProductoDTO dto){
        ProductoEX exUpdated = productoEXService.updateEX(code, dto);
        return ResponseEntity.ok(exUpdated);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<ProductoEX> delete(@PathVariable String code){
        productoEXService.deleteEX(code);
        return ResponseEntity.noContent().build();
    }
}
