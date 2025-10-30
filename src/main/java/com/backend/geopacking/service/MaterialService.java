package com.backend.geopacking.service;

import com.backend.geopacking.model.Material;

import java.util.List;

public interface MaterialService {
    List<Material> getAllMaterials();
    Material getMaterialByCode(String code);
    Material getMaterialByName(String name);
    Material createMaterial(Material material);
    Material updateMaterial(String code, Material materialDetails);
    void deleteMaterial(String code);
}
