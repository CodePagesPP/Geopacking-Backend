package com.backend.geopacking.controller;

import com.backend.geopacking.model.Color;
import com.backend.geopacking.service.ColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/colors")
public class ColorController {

    private final ColorService colorService;

    @Autowired
    public ColorController(ColorService colorService) { this.colorService = colorService; }

    @GetMapping
    public List<Color> getAll() { return colorService.getAllColors(); }

    @PostMapping
    public ResponseEntity<Color> save(@RequestBody Color color) {
        Color nuevoColor = colorService.createColor(color);
        return new ResponseEntity<>(nuevoColor,HttpStatus.CREATED);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Color> getColorByCode(@PathVariable String code) {
        return ResponseEntity.ok(colorService.getColorByCode(code));
    }

    @GetMapping("/nombre/{name}")
    public ResponseEntity<Color> getColorByName(@PathVariable String name) {
        return ResponseEntity.ok(colorService.getColorByName(name));
    }

    @PutMapping("/{code}")
    public ResponseEntity<Color> updateColor(@PathVariable String code, @RequestBody Color color) {
        Color colorUpdated = colorService.updateColor(code, color);
        return ResponseEntity.ok(colorUpdated);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteColor(@PathVariable String code) {
        colorService.deleteColor(code);
        return ResponseEntity.noContent().build();
    }
}
