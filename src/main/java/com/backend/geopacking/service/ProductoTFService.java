package com.backend.geopacking.service;

import com.backend.geopacking.dto.ProductoDTO;
import com.backend.geopacking.model.ProductoTF;

import java.util.List;

public interface ProductoTFService {
    List<ProductoTF> getAllTf();
    ProductoTF getTfByName(String name);
    ProductoTF getTfByCode(String code);
    ProductoTF createTf(ProductoDTO dto);
    ProductoTF updateTf(String code, ProductoDTO dto);
    void deleteTf(String code);
}
