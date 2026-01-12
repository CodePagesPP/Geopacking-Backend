package com.backend.geopacking.controller;

import com.backend.geopacking.dto.*;
import com.backend.geopacking.model.DetalleProduccionTF;
import com.backend.geopacking.model.InventarioCaja;
import com.backend.geopacking.model.OrdenTrabajoTF;
import com.backend.geopacking.repository.DetalleProduccionTFRepository;
import com.backend.geopacking.repository.OrdenTrabajoTFRepository;
import com.backend.geopacking.repository.UserRepository;
import com.backend.geopacking.service.OrdenTrabajoTFService;
import com.backend.geopacking.service.PdfTfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/orden-trabajo-tf")
public class OrdenTrabajoTFController {

    @Autowired
    private OrdenTrabajoTFService otService;
    @Autowired
    private PdfTfService pdfService;
    @Autowired
    private DetalleProduccionTFRepository detalleRepository;
    @Autowired
    private OrdenTrabajoTFRepository otRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/crear")
    public ResponseEntity<OrdenTrabajoTFDTO> crearOrden(@RequestBody OrdenTrabajoTFDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        OrdenTrabajoTFDTO nuevaOrden = otService.crearOrden(dto, userDetails.getUsername());
        return new ResponseEntity<>(nuevaOrden, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrdenTrabajoTFDTO>> listarOrdenes() {
        return new ResponseEntity<>(otService.listarOrdenes(), HttpStatus.OK);
    }

    @GetMapping("/ot")
    public ResponseEntity<List<OrdenTrabajoTFDTO>> listarOrdenesPendientes() {
        return new ResponseEntity<>(otService.listarOrdenesPendientes(), HttpStatus.OK);
    }

    @GetMapping("/list-pri")
    public ResponseEntity<List<OrdenTrabajoTFDTO>> listarOrdenesPrioridad() {
        return new ResponseEntity<>(otService.listarOrdenesPrioridad(), HttpStatus.OK);
    }

    @PutMapping("/ordenar")
    public ResponseEntity<Void> actualizarPrioridades(@RequestBody List<OrdenTrabajoTFDTO> listaOrdenada) {
        otService.actualizarPrioridades(listaOrdenada);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<OrdenTrabajoTFDTO> editarOrden(@PathVariable Long id, @RequestBody OrdenTrabajoTFDTO dto) {
        OrdenTrabajoTFDTO ordenActualizada = otService.actualizarOrden(id, dto);
        return new ResponseEntity<>(ordenActualizada, HttpStatus.OK);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarOrden(@PathVariable Long id) {
        otService.eliminarOrden(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/avance")
    public ResponseEntity<List<DetalleProduccionTF>> registrarAvance(@RequestBody List<RegistroProduccionTFDTO> dtos,
                                                                     @AuthenticationPrincipal UserDetails userDetails) {
        String username = (userDetails != null) ? userDetails.getUsername() : "ADMIN";

        List<DetalleProduccionTF> guardados = otService.registrarAvance(dtos, username);

        return ResponseEntity.ok(guardados);
    }

    @GetMapping("/etiquetas/{detalleId}")
    public ResponseEntity<byte[]> descargarEtiquetas(@PathVariable Long detalleId,
                                                     @RequestParam int inicioSecuencia) {
        try {
            DetalleProduccionTF detalle = detalleRepository.findById(detalleId)
                    .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));

            OrdenTrabajoTF ot = detalle.getOrdenTrabajo();

            byte[] pdfBytes = pdfService.generarEtiquetasPdf(ot, detalle, inicioSecuencia);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Etiquetas_" + ot.getCodigo() + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/reporte/{otId}")
    public ResponseEntity<byte[]> descargarReporte(@PathVariable Long otId, @RequestParam(required = false) String observaciones, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            OrdenTrabajoTF ot = otRepository.findById(otId)
                    .orElseThrow(() -> new RuntimeException("OT no encontrada"));

            String nombreOperador = "Desconocido";

            if (userDetails != null) {
                String dni = userDetails.getUsername();
                nombreOperador = userRepository.findByDni(dni)
                        .map(u -> u.getName() + " " + u.getLastName())
                        .orElse(dni);
            }

            List<DetalleProduccionTF> detalles = detalleRepository.findByOrdenTrabajoId(otId);

            byte[] pdfBytes = pdfService.generarReporteAvance(ot, detalles, observaciones, nombreOperador);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Reporte_" + ot.getCodigo() + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/buscar-bobina/{codigo}")
    public ResponseEntity<BobinaInfoDTO> buscarBobina(@PathVariable String codigo) {
        BobinaInfoDTO info = otService.buscarBobinaPorCodigo(codigo);
        return ResponseEntity.ok(info);
    }

    @PostMapping("/etiquetas/simular")
    public ResponseEntity<byte[]> simularEtiquetas(@RequestBody RegistroProduccionTFDTO dto,
                                                   @RequestParam int inicioSecuencia) {
        try {
            OrdenTrabajoTF ot = otRepository.findById(dto.getOtId())
                    .orElseThrow(() -> new RuntimeException("OT no encontrada"));

            DetalleProduccionTF detalleTemporal = DetalleProduccionTF.builder()
                    .loteBobina(dto.getLoteBobina())
                    .cajas(dto.getCajas())
                    .build();

            byte[] pdfBytes = pdfService.generarEtiquetasPdf(ot, detalleTemporal, inicioSecuencia);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Etiquetas_Preview.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/historial")
    public ResponseEntity<List<HistorialCajasDTO>> listarHistorial(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        return ResponseEntity.ok(otService.listarHistorial(fechaInicio, fechaFin));
    }

    @GetMapping("/inventario/tf")
    public ResponseEntity<List<InventarioCajaDTO>> listarInventarioTF() {
        return ResponseEntity.ok(otService.listarInventarioPorEstado("EN_TF"));
    }

    @GetMapping("/inventario/pt")
    public ResponseEntity<List<InventarioCajaDTO>> listarInventarioPT() {
        return ResponseEntity.ok(otService.listarInventarioPorEstado("EN_PT"));
    }

    @PostMapping("/inventario/mover-a-pt/{id}")
    public ResponseEntity<Void> moverAProductosTerminados(@PathVariable Long id) {
        otService.enviarAProductosTerminados(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inventario/registrar-salida")
    public ResponseEntity<byte[]> registrarSalida(@RequestBody SalidaRequestDTO request,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        String username = (userDetails != null) ? userDetails.getUsername() : "ADMIN";

        byte[] pdfBytes = otService.registrarSalidaMasiva(request, username);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Salida.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/inventario/buscar-producto")
    public ResponseEntity<List<InventarioCajaDTO>> buscarPorCodigo(@RequestParam("codigo") String codigo) {
        List<InventarioCajaDTO> encontrados = otService.buscarInventarioPorCodigoProducto(codigo);
        return ResponseEntity.ok(encontrados);
    }
}
