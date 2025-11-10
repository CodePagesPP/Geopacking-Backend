package com.backend.geopacking.service;

import com.backend.geopacking.model.ProductoEX;

import java.util.List;

public interface ProductoEXService {
    List<ProductoEX> getAllEx();
    ProductoEX getExByName(String name);
    ProductoEX getExByCode(String code);
    ProductoEX createEX(ProductoEX ex);
    ProductoEX updateEX(ProductoEX ex, String code);
    void deleteEX(String code);
}
