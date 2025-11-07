package com.backend.geopacking.controller;

import com.backend.geopacking.dto.MolinoDTO;
import com.backend.geopacking.model.*;
import com.backend.geopacking.service.MaquinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maquinas")
public class MaquinaController {

    @Autowired
    private MaquinaService maquinaService;


    @GetMapping
    public ResponseEntity<List<Maquina>> getAllMaquinas() {

        return ResponseEntity.ok(maquinaService.getAllMaquinas());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<Maquina>> getMaquinasActivas() {
        return ResponseEntity.ok(maquinaService.getAllMaquinasActivas());
    }

    @GetMapping("/molinos-activos")
    public ResponseEntity<List<Maquina>> getMolinosActivos() {
        List<Maquina> molinos = maquinaService.getMolinosActivos();
        return ResponseEntity.ok(molinos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Maquina> getMaquinaById(@PathVariable Long id) {

        return ResponseEntity.ok(maquinaService.getMaquinaById(id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaquina(@PathVariable Long id) {
        maquinaService.deleteMaquina(id);

        return ResponseEntity.noContent().build();
    }


    @PostMapping("/extrusora")
    public ResponseEntity<Extrusora> createExtrusora(@RequestBody Extrusora extrusora) {
        Extrusora created = maquinaService.createExtrusora(extrusora);

        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/termoformadora")
    public ResponseEntity<Termoformadora> createTermoformadora(@RequestBody Termoformadora termoformadora) {
        Termoformadora created = maquinaService.createTermoformadora(termoformadora);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/molino")
    public ResponseEntity<Molino> createMolino(@RequestBody MolinoDTO molino) {
        Molino created = maquinaService.createMolino(molino);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }



    @PutMapping("/extrusora/{id}")
    public ResponseEntity<Extrusora> updateExtrusora(@PathVariable Long id, @RequestBody Extrusora details) {

        return ResponseEntity.ok(maquinaService.updateExtrusora(id, details));
    }

    @PutMapping("/termoformadora/{id}")
    public ResponseEntity<Termoformadora> updateTermoformadora(@PathVariable Long id, @RequestBody Termoformadora details) {
        return ResponseEntity.ok(maquinaService.updateTermoformadora(id, details));
    }

    @PutMapping("/molino/{id}")
    public ResponseEntity<Molino> updateMolino(@PathVariable Long id, @RequestBody MolinoDTO details) {
        return ResponseEntity.ok(maquinaService.updateMolino(id, details));
    }
}
