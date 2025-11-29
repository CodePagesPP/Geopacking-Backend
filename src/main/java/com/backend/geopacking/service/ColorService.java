package com.backend.geopacking.service;

import com.backend.geopacking.model.Color;

import java.util.List;

public interface ColorService {
    List<Color> getAllColors();
    Color getColorByName(String name);
    Color getColorByCode(String code);
    Color createColor(Color color);
    Color updateColor(Long id, Color color);
    void deleteColor(Long id);
}
