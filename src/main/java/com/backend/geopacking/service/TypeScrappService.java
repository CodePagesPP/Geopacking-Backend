package com.backend.geopacking.service;

import com.backend.geopacking.model.TypeScrapp;

import java.util.List;

public interface TypeScrappService {
    public List<TypeScrapp> getAllTypeScrapps();
    public TypeScrapp getTypeScrappsByName(String name);
    public TypeScrapp getTypeScrappsByCode(String code);
    public TypeScrapp createTypeScrapps(TypeScrapp origen);
    public TypeScrapp updateTypeScrapps(String code, TypeScrapp origen);
    public void deleteTypeScrapps(String code);
}
