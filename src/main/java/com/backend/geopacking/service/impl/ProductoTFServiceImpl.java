package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.ProductoDTO;
import com.backend.geopacking.exceptions.ResourceNotFoundException;
import com.backend.geopacking.model.Color;
import com.backend.geopacking.model.ProductoEX;
import com.backend.geopacking.model.ProductoTF;
import com.backend.geopacking.model.Products;
import com.backend.geopacking.repository.ColorRepository;
import com.backend.geopacking.repository.ProductoEXRepository;
import com.backend.geopacking.repository.ProductoTFRepository;
import com.backend.geopacking.service.ProductoTFService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class ProductoTFServiceImpl implements ProductoTFService {

    private final ProductoTFRepository productoTFRepository;
    private final ProductoEXRepository productoEXRepository;
    private final ColorRepository colorRepository;

    @Override
    public List<ProductoTF> getAllTf() { return productoTFRepository.findAll(); }

    @Override
    public ProductoTF getTfByName(String name) {
        return productoTFRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Producto TF no encontrado: " + name));
    }

    @Override
    public ProductoTF getTfByCode(String code) {
        return productoTFRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Producto TF no encontrado: " + code));
    }

    @Override
    public ProductoTF createTf(ProductoDTO dto) {
        ProductoTF productoTF = new ProductoTF();
        mapCommonFields(dto, productoTF);

        if (dto.getMaterialId() != null) {
            ProductoEX productoBase = productoEXRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto Base (EX) no encontrado ID: " + dto.getMaterialId()));
            productoTF.setProductoBase(productoBase);
        }

        return productoTFRepository.save(productoTF);
    }

    @Override
    public ProductoTF updateTf(String code, ProductoDTO dto) {
        ProductoTF productoTF = getTfByCode(code);
        mapCommonFields(dto, productoTF);

        if (dto.getMaterialId() != null) {
            ProductoEX productoBase = productoEXRepository.findById(dto.getMaterialId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto Base (EX) no encontrado ID: " + dto.getMaterialId()));
            productoTF.setProductoBase(productoBase);
        } else {
            productoTF.setProductoBase(null);
        }

        return productoTFRepository.save(productoTF);
    }

    @Override
    public void deleteTf(String code) {
        ProductoTF productoTF = getTfByCode(code);
        productoTFRepository.delete(productoTF);
    }

    private void mapCommonFields(ProductoDTO dto, Products entity) {
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setReferencia(dto.getReferencia());
        entity.setMarca(dto.getMarca());
        entity.setLinea(dto.getLinea());
        entity.setCategoria(dto.getCategoria());
        entity.setUnidadDeMedida(dto.getUnidadDeMedida());
        entity.setPesoUnitario(dto.getPesoUnitario());
        entity.setActivo(dto.isActivo());

        if (dto.getColorId() != null) {
            Color color = colorRepository.findById(dto.getColorId())
                    .orElseThrow(() -> new EntityNotFoundException("Color no encontrado ID: " + dto.getColorId()));
            entity.setColor(color);
        } else {
            entity.setColor(null);
        }
    }
}