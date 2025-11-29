package com.backend.geopacking.service.impl;

import com.backend.geopacking.exceptions.ResourceNotFoundException;
import com.backend.geopacking.model.Color;
import com.backend.geopacking.repository.ColorRepository;
import com.backend.geopacking.service.ColorService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ColorServiceImpl implements ColorService {

    @Autowired
    private final ColorRepository colorRepository;

    public ColorServiceImpl(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    @Override
    public List<Color> getAllColors() {
        return colorRepository.findAll();
    }

    @Override
    public Color getColorByName(String name) {
        return colorRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Color no encontrado con nombre: " + name));
    }

    @Override
    public Color getColorByCode(String code) {
        return colorRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Color no encontrado con codigo: " + code));
    }

    @Override
    public Color createColor(Color color) {
        return colorRepository.save(color);
    }

    @Override
    public Color updateColor(Long id, Color color) {
        Color colorFound = colorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Color no encontrado con codigo: " + id));

        if(color.getCode() != null) {
            colorFound.setCode(color.getCode());
        }

        if(color.getName() != null) {
            colorFound.setName(color.getName());
        }

        return colorRepository.save(colorFound);
    }

    @Override
    public void deleteColor(Long id) {
        Color colorFound = colorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Color no encontrado con codigo: " + id));

        colorRepository.delete(colorFound);
    }
}
