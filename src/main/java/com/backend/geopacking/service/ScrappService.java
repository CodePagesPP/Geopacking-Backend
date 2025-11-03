package com.backend.geopacking.service;

import com.backend.geopacking.dto.ScrappDTO;
import com.backend.geopacking.model.Scrapp;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface ScrappService {
    Scrapp registrarScrapp(ScrappDTO dto, UserDetails userDetails);

    List<Scrapp> obtenerTodos();

    Scrapp obtenerPorId(Long id);
}
