package com.backend.geopacking.controller;

import com.backend.geopacking.dto.ScrappDTO;
import com.backend.geopacking.model.Scrapp;
import com.backend.geopacking.service.EtiquetaService;
import com.backend.geopacking.service.ScrappService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scrapp")
@RequiredArgsConstructor
public class ScrappController {
    private final ScrappService scrappService;
    private final EtiquetaService etiquetaService;
    @PostMapping("/registrar")
    public ResponseEntity<Scrapp> registrarPesaje(
            @RequestBody ScrappDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Scrapp nuevoRegistro = scrappService.registrarScrapp(dto, userDetails);
        return ResponseEntity.ok(nuevoRegistro);
    }

    @GetMapping
    public ResponseEntity<List<Scrapp>> obtenerTodosLosRegistros() {
        return ResponseEntity.ok(scrappService.obtenerTodos());
    }

    @GetMapping("/etiqueta/{registroId}")
    public ResponseEntity<byte[]> generarEtiqueta(
            @PathVariable Long registroId
    ) throws Exception {


        byte[] pdfBytes = etiquetaService.generarEtiquetaScrapp(registroId);


        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=etiqueta_" + registroId + ".pdf");
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
