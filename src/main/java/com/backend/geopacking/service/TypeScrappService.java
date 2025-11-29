package com.backend.geopacking.service;

import com.backend.geopacking.model.TypeScrapp;

import java.util.List;

public interface TypeScrappService {
    List<TypeScrapp> getAllTypeScrapps();
    TypeScrapp getTypeScrappsByName(String name);
    TypeScrapp getTypeScrappsByCode(String code);
    TypeScrapp createTypeScrapps(TypeScrapp origen);
    TypeScrapp updateTypeScrapps(Long id, TypeScrapp origen);
    void deleteTypeScrapps(Long id);
}
