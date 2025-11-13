package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.ProductoDTO;
import com.backend.geopacking.exceptions.ResourceNotFoundException;
import com.backend.geopacking.model.Color;
import com.backend.geopacking.model.Material;
import com.backend.geopacking.model.ProductoTF;
import com.backend.geopacking.model.Products;
import com.backend.geopacking.repository.ColorRepository;
import com.backend.geopacking.repository.MaterialRepository;
import com.backend.geopacking.repository.ProductoTFRepository;
import com.backend.geopacking.service.ProductoTFService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class ProductoTFServiceImpl implements ProductoTFService {


    private final ProductoTFRepository productoTFRepository;
    private final MaterialRepository materialRepository;
    private final ColorRepository colorRepository;

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
    public ProductoTF createTf(ProductoDTO dto) {
        ProductoTF productoTF = new ProductoTF();
        mapDtoToEntity(dto, productoTF);
        return productoTFRepository.save(productoTF);
    }
    @Override
    public ProductoTF updateTf(String code, ProductoDTO dto) {
        ProductoTF productoTF = productoTFRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con codigo: " + code));

        mapDtoToEntity(dto, productoTF);

        return productoTFRepository.save(productoTF);
    }

    @Override
    public void deleteTf(String code) {
        ProductoTF productoTF = productoTFRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con codigo: " + code));

        productoTFRepository.delete(productoTF);
    }

    private void mapDtoToEntity(ProductoDTO dto, Products entity) {
        if (dto.getMaterialId() != null) {
            Material material = materialRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new EntityNotFoundException("Material no encontrado con ID: " + dto.getMaterialId()));
            entity.setMaterial(material);
        } else {
            entity.setMaterial(null);
        }

        if (dto.getColorId() != null) {
            Color color = colorRepository.findById(dto.getColorId())
                    .orElseThrow(() -> new EntityNotFoundException("Color no encontrado con ID: " + dto.getColorId()));
            entity.setColor(color);
        } else {
            entity.setColor(null);
        }

        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setReferencia(dto.getReferencia());
        entity.setMarca(dto.getMarca());
        entity.setLinea(dto.getLinea());
        entity.setCategoria(dto.getCategoria());
        entity.setUnidadDeMedida(dto.getUnidadDeMedida());
        entity.setPesoUnitario(dto.getPesoUnitario());
        entity.setActivo(dto.isActivo());
    }
}
