package com.backend.geopacking.service;

import com.backend.geopacking.model.ProductoTF;

import java.util.List;

public interface ProductoTFService {
    List<ProductoTF> getAllTf();
    ProductoTF getTfByName(String name);
    ProductoTF getTfByCode(String code);
    ProductoTF createTf(ProductoTF tf);
    ProductoTF updateTf(String code, ProductoTF tf);
    void deleteTf(String code);
}
