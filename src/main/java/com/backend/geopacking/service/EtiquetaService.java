package com.backend.geopacking.service;

import org.springframework.stereotype.Service;

@Service
public interface EtiquetaService {
    byte[] generarEtiquetaScrapp(
            Long registroId
    ) throws Exception;
}
