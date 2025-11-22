package com.backend.geopacking.controller;

import com.backend.geopacking.dto.OperadorDTO;
import com.backend.geopacking.dto.UserDTO;

import com.backend.geopacking.repository.MaquinaRepository;
import com.backend.geopacking.repository.UserRepository;
import com.backend.geopacking.service.EtiquetaService;
import com.backend.geopacking.service.OperadorService;
import com.itextpdf.text.*;

import com.itextpdf.text.pdf.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;


import java.awt.*;

import java.util.List;

@RestController
@RequestMapping("/operadores")
@RequiredArgsConstructor
public class OperadorController {

    private final OperadorService operadorService;
    @PostMapping
    public ResponseEntity<UserDTO> registerOperador(@RequestBody OperadorDTO operadorDTO) {
        return new ResponseEntity<>(operadorService.registerOperador(operadorDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllOperadores() {
        return ResponseEntity.ok(operadorService.getAllOperadores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getOperadorById(@PathVariable long id) {
        return ResponseEntity.ok(operadorService.getOperadorById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateOperador(@PathVariable long id, @RequestBody OperadorDTO operadorDTO) {
        return ResponseEntity.ok(operadorService.updateOperador(id, operadorDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperador(@PathVariable long id) {
        operadorService.deleteOperador(id);
        return ResponseEntity.noContent().build();
    }

}
