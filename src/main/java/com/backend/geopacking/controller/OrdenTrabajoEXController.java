package com.backend.geopacking.controller;

import com.backend.geopacking.dto.OrdenTrabajoEXDTO;
import com.backend.geopacking.service.OrdenTrabajoEXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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

    @GetMapping()
    public ResponseEntity<List<OrdenTrabajoEXDTO>> listarOrdenes() {
        List<OrdenTrabajoEXDTO> lista = ordenTrabajoEXService.listarOrdenes();
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }
}
