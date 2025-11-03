package com.backend.geopacking.service;

import com.backend.geopacking.model.Extrusora;
import com.backend.geopacking.model.Maquina;
import com.backend.geopacking.model.Molino;
import com.backend.geopacking.model.Termoformadora;

import java.util.List;
import java.util.Optional;

public interface MaquinaService {

    List<Maquina> getAllMaquinas();
    List<Maquina> getAllMaquinasActivas();
    Maquina getMaquinaById(Long id);


    Extrusora createExtrusora(Extrusora extrusora);
    Termoformadora createTermoformadora(Termoformadora termoformadora);
    Molino createMolino(Molino molino);


    Extrusora updateExtrusora(Long id, Extrusora details);
    Termoformadora updateTermoformadora(Long id, Termoformadora details);
    Molino updateMolino(Long id, Molino details);


    void deleteMaquina(Long id);
}