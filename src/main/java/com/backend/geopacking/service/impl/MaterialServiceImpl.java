package com.backend.geopacking.service.impl;

import com.backend.geopacking.exceptions.ResourceNotFoundException;
import com.backend.geopacking.model.Material;
import com.backend.geopacking.repository.MaterialRepository;
import com.backend.geopacking.service.MaterialService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class MaterialServiceImpl implements MaterialService {

    @Autowired
    private final MaterialRepository materialRepository;


    public MaterialServiceImpl(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }

    @Override
    public Material getMaterialByCode(String code) {
        return materialRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con codigo: " + code));
    }

    @Override
    public Material getMaterialByName(String name) {
        return materialRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con nombre: " + name));
    }

    @Override
    public Material createMaterial(Material material) {
        return materialRepository.save(material);
    }

    @Override
    public Material updateMaterial(Long id, Material materialDetails) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material no encontrado con id: " + id));

        if (materialDetails.getName() != null) {
            material.setName(materialDetails.getName());
        }

        if (materialDetails.getCode() != null) {
            material.setCode(materialDetails.getCode());
        }

        return materialRepository.save(material);
    }

    @Override
    public void deleteMaterial(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material no encontrado con codigo: " + id));

        materialRepository.delete(material);
    }
}