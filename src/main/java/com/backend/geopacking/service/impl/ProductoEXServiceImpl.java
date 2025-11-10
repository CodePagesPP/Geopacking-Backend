package com.backend.geopacking.service.impl;

import com.backend.geopacking.exceptions.ResourceNotFoundException;
import com.backend.geopacking.model.ProductoEX;
import com.backend.geopacking.repository.ProductoEXRepository;
import com.backend.geopacking.service.ProductoEXService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ProductoEXServiceImpl implements ProductoEXService {

    @Autowired
    private final ProductoEXRepository productoEXRepository;

    ProductoEXServiceImpl(ProductoEXRepository productoEXRepository) { this.productoEXRepository = productoEXRepository; }

    @Override
    public List<ProductoEX> getAllEx() { return productoEXRepository.findAll(); }

    @Override
    public ProductoEX getExByName(String name) {
        return productoEXRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con nombre: " + name));
    }

    @Override
    public ProductoEX getExByCode(String code) {
        return productoEXRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con codigo: " + code));
    }

    @Override
    public ProductoEX createEX(ProductoEX ex) { return productoEXRepository.save(ex); }

    @Override
    public ProductoEX updateEX(ProductoEX ex, String code) {
        ProductoEX productoEX = productoEXRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con codigo: " + code));

        if (ex.getName() != null) {
            productoEX.setName(ex.getName());
        }

        if (ex.getCode() != null) {
            productoEX.setCode(ex.getCode());
        }

        return productoEXRepository.save(productoEX);
    }

    @Override
    public void deleteEX(String code) {
        ProductoEX productoEX = productoEXRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con codigo: " + code));

        productoEXRepository.delete(productoEX);
    }
}
