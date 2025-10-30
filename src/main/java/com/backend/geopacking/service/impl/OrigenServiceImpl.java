package com.backend.geopacking.service.impl;

import com.backend.geopacking.model.Origen;
import com.backend.geopacking.repository.OrigenRepository;
import com.backend.geopacking.service.OrigenService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class OrigenServiceImpl implements OrigenService {

    @Autowired
    private final OrigenRepository origenRepository;

    public OrigenServiceImpl(OrigenRepository origenRepository) { this.origenRepository = origenRepository; }

    @Override
    public List<Origen> getAllOrigins() {
        return origenRepository.findAll();
    }

    @Override
    public Origen getOriginByName(String name) {
        return origenRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Origen no encontrado con nombre: " + name));
    }

    @Override
    public Origen getOriginByCode(String code) {
        return origenRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Origen no encontrado con codigo: " + code));
    }

    @Override
    public Origen createOrigin(Origen origen) {
        return origenRepository.save(origen);
    }

    @Override
    public Origen updateOrigin(String code, Origen origen) {
        Origen origenFound = origenRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Origen no encontrado con codigo: " + code));

        if (origen.getName() != null) {
            origenFound.setName(origen.getName());
        }

        if (origen.getCode() != null) {
            origenFound.setCode(origen.getCode());
        }

        return origenRepository.save(origenFound);
    }

    @Override
    public void deleteOrigin(String code) {
        Origen origenFound = origenRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Origen no encontrado con codigo: " + code));

        origenRepository.delete(origenFound);
    }
}
