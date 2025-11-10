package com.backend.geopacking.service.impl;

import com.backend.geopacking.exceptions.ResourceNotFoundException;
import com.backend.geopacking.model.ProductoTF;
import com.backend.geopacking.repository.ProductoTFRepository;
import com.backend.geopacking.service.ProductoTFService;
import jakarta.transaction.Transactional; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ProductoTFServiceImpl implements ProductoTFService {

    @Autowired
    private final ProductoTFRepository productoTFRepository;

    public ProductoTFServiceImpl(ProductoTFRepository productoTFRepository) { this.productoTFRepository = productoTFRepository; }

    @Override
    public List<ProductoTF> getAllTf() { return productoTFRepository.findAll(); }

    @Override
    public ProductoTF getTfByName(String name) {
        return productoTFRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con nombre: " + name));
    }

    @Override
    public ProductoTF getTfByCode(String code) {
        return productoTFRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con codigo: " + code));
    }

    @Override
    public ProductoTF createTf(ProductoTF tf) { return productoTFRepository.save(tf); }

    @Override
    public ProductoTF updateTf(String code, ProductoTF tf) {
        ProductoTF productoTF = productoTFRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con codigo: " + code));

        if (tf.getName() != null) {
            productoTF.setName(tf.getName());
        }

        if (tf.getCode() != null) {
            productoTF.setCode(tf.getCode());
        }

        return productoTFRepository.save(productoTF);
    }

    @Override
    public void deleteTf(String code) {
        ProductoTF productoTF = productoTFRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con codigo: " + code));

        productoTFRepository.delete(productoTF);
    }
}
