package com.backend.geopacking.service;

import com.backend.geopacking.dto.ProductoDTO;
import com.backend.geopacking.model.ProductoEX;

import java.util.List;

public interface ProductoEXService {
    List<ProductoEX> getAllEx();
    ProductoEX getExByName(String name);
    ProductoEX getExByCode(String code);
    ProductoEX createEX(ProductoDTO dto);
    ProductoEX updateEX(String code, ProductoDTO dto);
    void deleteEX(String code);
}
