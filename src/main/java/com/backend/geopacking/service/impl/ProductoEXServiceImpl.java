package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.ProductoDTO;
import com.backend.geopacking.exceptions.ResourceNotFoundException;
import com.backend.geopacking.model.Color;
import com.backend.geopacking.model.Material;
import com.backend.geopacking.model.ProductoEX;
import com.backend.geopacking.model.Products;
import com.backend.geopacking.repository.ColorRepository;
import com.backend.geopacking.repository.MaterialRepository;
import com.backend.geopacking.repository.ProductoEXRepository;
import com.backend.geopacking.service.ProductoEXService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class ProductoEXServiceImpl implements ProductoEXService {


    private final ProductoEXRepository productoEXRepository;
    private final MaterialRepository materialRepository; // <-- Inyecta
    private final ColorRepository colorRepository;


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
    public ProductoEX createEX(ProductoDTO dto) {

        ProductoEX productoEX = new ProductoEX();
        mapDtoToEntity(dto, productoEX);
        return productoEXRepository.save(productoEX);
    }

    @Override
    public ProductoEX updateEX(String code, ProductoDTO dto) {
        ProductoEX productoEX = productoEXRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con codigo: " + code));


        mapDtoToEntity(dto, productoEX);

        return productoEXRepository.save(productoEX);
    }

    @Override
    public void deleteEX(String code) {
        ProductoEX productoEX = productoEXRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con codigo: " + code));

        productoEXRepository.delete(productoEX);
    }

    private void mapDtoToEntity(ProductoDTO dto, Products entity) {

        if (dto.getMaterialesIds() != null && !dto.getMaterialesIds().isEmpty()) {

            List<Material> listaMateriales = materialRepository.findAllById(dto.getMaterialesIds());

            if(listaMateriales.size() != dto.getMaterialesIds().size()) {

                throw new EntityNotFoundException("Algunos materiales no existen");
            }

            ((ProductoEX) entity).setMateriales(listaMateriales);
        } else {
            ((ProductoEX) entity).setMateriales(Collections.emptyList());
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
        entity.setActivo(dto.isActivo());
    }
}
