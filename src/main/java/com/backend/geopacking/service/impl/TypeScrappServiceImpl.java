package com.backend.geopacking.service.impl;

import com.backend.geopacking.model.TypeScrapp;
import com.backend.geopacking.repository.TypeScrappRepository;
import com.backend.geopacking.service.TypeScrappService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
@Transactional
public class TypeScrappServiceImpl implements TypeScrappService {
    
    private final TypeScrappRepository typeScrappRepository;

    
    @Override
    public List<TypeScrapp> getAllTypeScrapps() {
        return typeScrappRepository.findAll();
    }

    @Override
    public TypeScrapp getTypeScrappsByName(String name) {
        return typeScrappRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Tipo de scrapp  no encontrado con nombre: " + name));
    }

    @Override
    public TypeScrapp getTypeScrappsByCode(String code) {
        return typeScrappRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Tipo de scrapp  no encontrado con codigo: " + code));
    }

    @Override
    public TypeScrapp createTypeScrapps(TypeScrapp origen) {
        return typeScrappRepository.save(origen);
    }

    @Override
    public TypeScrapp updateTypeScrapps(Long id, TypeScrapp origen) {
        TypeScrapp origenFound = typeScrappRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de scrapp  no encontrado con codigo: " + id));

        if (origen.getName() != null) {
            origenFound.setName(origen.getName());
        }

        if (origen.getCode() != null) {
            origenFound.setCode(origen.getCode());
        }

        return typeScrappRepository.save(origenFound);
    }

    @Override
    public void deleteTypeScrapps(Long id) {
        TypeScrapp origenFound = typeScrappRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de scrapp no encontrado con codigo: " + id));

        typeScrappRepository.delete(origenFound);
    }
}
