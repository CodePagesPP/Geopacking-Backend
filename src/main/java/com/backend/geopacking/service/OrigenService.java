package com.backend.geopacking.service;

import com.backend.geopacking.model.Origen;

import java.util.List;

public interface OrigenService {
    List<Origen> getAllOrigins();
    Origen getOriginByName(String name);
    Origen getOriginByCode(String code);
    Origen createOrigin(Origen origen);
    Origen updateOrigin(Long id, Origen origen);
    void deleteOrigin(Long id);
}
