package com.backend.geopacking.service.impl;

import com.backend.geopacking.exceptions.ResourceNotFoundException;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.MaquinaRepository;
import com.backend.geopacking.service.MaquinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MaquinaServiceImpl implements MaquinaService {

    @Autowired
    private MaquinaRepository maquinaRepository;

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
    public Molino createMolino(Molino molino) {
        return maquinaRepository.save(molino);
    }

    // --- UPDATE ---

    @Override
    public Extrusora updateExtrusora(Long id, Extrusora details) {
        Maquina maquina = getMaquinaById(id); // Reutiliza el método que ya lanza excepción

        if (!(maquina instanceof Extrusora)) {
            throw new IllegalArgumentException("La máquina con id " + id + " no es una Extrusora.");
        }

        Extrusora existing = (Extrusora) maquina;
        // Actualizar campos comunes
        existing.setCodigo(details.getCodigo());
        existing.setMarca(details.getMarca());
        existing.setModelo(details.getModelo());
        existing.setActivo(details.isActivo());
        // Actualizar campo único
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
        existing.setMarca(details.getMarca());
        existing.setModelo(details.getModelo());
        existing.setActivo(details.isActivo());
        existing.setAreaDeFormado(details.getAreaDeFormado());

        return maquinaRepository.save(existing);
    }

    @Override
    public Molino updateMolino(Long id, Molino details) {
        Maquina maquina = getMaquinaById(id);

        if (!(maquina instanceof Molino)) {
            throw new IllegalArgumentException("La máquina con id " + id + " no es un Molino.");
        }

        Molino existing = (Molino) maquina;
        existing.setCodigo(details.getCodigo());
        existing.setMarca(details.getMarca());
        existing.setModelo(details.getModelo());
        existing.setActivo(details.isActivo());

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