package com.backend.geopacking.controller;

import com.backend.geopacking.dto.OrdenTrabajoTFDTO;
import com.backend.geopacking.service.OrdenTrabajoTFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orden-trabajo-tf")
public class OrdenTrabajoTFController {

    @Autowired
    private OrdenTrabajoTFService otService;

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
}
