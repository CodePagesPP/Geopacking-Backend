package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.MolinoDTO;
import com.backend.geopacking.exceptions.ResourceNotFoundException;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.MaquinaRepository;
import com.backend.geopacking.repository.OrigenRepository;
import com.backend.geopacking.service.MaquinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class MaquinaServiceImpl implements MaquinaService {

    @Autowired
    private MaquinaRepository maquinaRepository;
    @Autowired
    private OrigenRepository origenRepository;
    // --- READ ---

    @Override
    public List<Maquina> getAllMaquinas() {
        return maquinaRepository.findAll();
    }

    @Override
    public List<Maquina> getAllMaquinasActivas() {
        return maquinaRepository.findByActivo(true);
    }

    @Override
    public Maquina getMaquinaById(Long id) {
        return maquinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maquina no encontrada con id: " + id));
    }

    @Override
    public List<Maquina> getMolinosActivos() {

        return maquinaRepository.findMolinosActivos(true);
    }

    // --- CREATE ---

    @Override
    public Extrusora createExtrusora(Extrusora extrusora) {
        return maquinaRepository.save(extrusora);
    }

    @Override
    public Termoformadora createTermoformadora(Termoformadora termoformadora) {
        return maquinaRepository.save(termoformadora);
    }

    @Override
    public Molino createMolino(MolinoDTO dto) {
        // 1. Buscar los orígenes reales desde la DB
        Set<Origen> managedOrigenes = new HashSet<>();
        if (dto.getOrigenIds() != null && !dto.getOrigenIds().isEmpty()) {
            managedOrigenes = new HashSet<>(origenRepository.findAllById(dto.getOrigenIds()));
        }

        // 2. Construir el nuevo Molino
        Molino molino = Molino.builder()
                .codigo(dto.getCodigo())
                .nroSerie(dto.getNroSerie())
                .marca(dto.getMarca())
                .modelo(dto.getModelo())
                .activo(dto.isActivo())
                .origenes(managedOrigenes)
                .build();

        // 3. Guardar
        return maquinaRepository.save(molino);
    }

    // --- UPDATE ---

    @Override
    public Extrusora updateExtrusora(Long id, Extrusora details) {
        Maquina maquina = getMaquinaById(id);

        if (!(maquina instanceof Extrusora)) {
            throw new IllegalArgumentException("La máquina con id " + id + " no es una Extrusora.");
        }

        Extrusora existing = (Extrusora) maquina;
        existing.setCodigo(details.getCodigo());
        existing.setNroSerie(details.getNroSerie());
        existing.setMarca(details.getMarca());
        existing.setModelo(details.getModelo());
        existing.setActivo(details.isActivo());
        existing.setRendimiento(details.getRendimiento());

        return maquinaRepository.save(existing);
    }

    @Override
    public Termoformadora updateTermoformadora(Long id, Termoformadora details) {
        Maquina maquina = getMaquinaById(id);

        if (!(maquina instanceof Termoformadora)) {
            throw new IllegalArgumentException("La máquina con id " + id + " no es una Termoformadora.");
        }

        Termoformadora existing = (Termoformadora) maquina;
        existing.setCodigo(details.getCodigo());
        existing.setNroSerie(details.getNroSerie());
        existing.setMarca(details.getMarca());
        existing.setModelo(details.getModelo());
        existing.setActivo(details.isActivo());
        existing.setAreaDeFormado(details.getAreaDeFormado());

        return maquinaRepository.save(existing);
    }

    @Override
    public Molino updateMolino(Long id, MolinoDTO dto) {
        // 1. Encontrar el molino existente
        Molino existing = (Molino) maquinaRepository.findById(id)
                .filter(m -> m instanceof Molino)
                .orElseThrow(() -> new IllegalArgumentException("Molino con id " + id + " no encontrado."));

        // 2. Actualizar campos base
        existing.setCodigo(dto.getCodigo());
        existing.setNroSerie(dto.getNroSerie());
        existing.setMarca(dto.getMarca());
        existing.setModelo(dto.getModelo());
        existing.setActivo(dto.isActivo());

        // 3. Actualizar orígenes... (igual)
        Set<Origen> managedOrigenes = new HashSet<>();
        if (dto.getOrigenIds() != null && !dto.getOrigenIds().isEmpty()) {
            managedOrigenes = new HashSet<>(origenRepository.findAllById(dto.getOrigenIds()));
        }
        existing.setOrigenes(managedOrigenes);

        return maquinaRepository.save(existing);
    }

    // --- DELETE ---

    @Override
    public void deleteMaquina(Long id) {
        if (!maquinaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Maquina no encontrada con id: " + id);
        }
        maquinaRepository.deleteById(id);
    }
}