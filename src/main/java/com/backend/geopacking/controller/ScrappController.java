package com.backend.geopacking.controller;

import com.backend.geopacking.dto.PaginatedScrappReportDTO;
import com.backend.geopacking.dto.ScrappDTO;
import com.backend.geopacking.dto.ScrappReportDTO;
import com.backend.geopacking.model.Scrapp;
import com.backend.geopacking.service.EtiquetaService;
import com.backend.geopacking.service.ScrappService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping("/reporte-admin")
    public ResponseEntity<PaginatedScrappReportDTO> obtenerReporteAdmin(

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,


            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @AuthenticationPrincipal UserDetails userDetails) {


        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        PaginatedScrappReportDTO reporte = scrappService.obtenerReporteAdmin(pageable, fechaInicio, fechaFin, userDetails);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/reporte-completo-pdf")
    public ResponseEntity<byte[]> imprimirReporteCompletoA4(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        byte[] pdfBytes = scrappService.generarReporteCompletoPdf(fechaInicio, fechaFin);

        if (pdfBytes.length == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar el PDF".getBytes());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=Reporte_Scrapp_Completo.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
