package com.backend.geopacking.controller;

import com.backend.geopacking.dto.OrdenTrabajoEXDTO;
import com.backend.geopacking.service.OrdenTrabajoEXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/orden-trabajo-ex")
public class OrdenTrabajoEXController {

    @Autowired
    private OrdenTrabajoEXService ordenTrabajoEXService;

    @PostMapping("/crear")
    public ResponseEntity<OrdenTrabajoEXDTO> crearOrden(@RequestBody OrdenTrabajoEXDTO dto, Principal principal) {
        OrdenTrabajoEXDTO nuevaOrden = ordenTrabajoEXService.crearOrden(dto, principal.getName());
        return new ResponseEntity<>(nuevaOrden, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<OrdenTrabajoEXDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long maquinaId,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta
    ) {
        // Ordenar por ID descendente por defecto
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        return ResponseEntity.ok(ordenTrabajoEXService.listarPaginado(maquinaId, productoId, estado, fechaDesde, fechaHasta, pageable));
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<OrdenTrabajoEXDTO> editarOrden(@PathVariable Long id, @RequestBody OrdenTrabajoEXDTO dto) {
        OrdenTrabajoEXDTO ordenActualizada = ordenTrabajoEXService.editarOrden(id, dto);
        return new ResponseEntity<>(ordenActualizada, HttpStatus.OK);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarOrden(@PathVariable Long id) {
        ordenTrabajoEXService.eliminarOrden(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @GetMapping("/ot")
    public ResponseEntity<List<OrdenTrabajoEXDTO>> listarOrdenesot() {
        List<OrdenTrabajoEXDTO> lista = ordenTrabajoEXService.listarOrdenesOT();
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    @GetMapping("/list-pri")
    public ResponseEntity<List<OrdenTrabajoEXDTO>> listarOrdenesPrioridad() {
        List<OrdenTrabajoEXDTO> lista = ordenTrabajoEXService.listarOrdenesPrioridad();
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }


    @PutMapping("/ordenar")
    public ResponseEntity<Void> actualizarPrioridades(@RequestBody List<OrdenTrabajoEXDTO> listaOrdenada) {
        ordenTrabajoEXService.actualizarPrioridades(listaOrdenada);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
