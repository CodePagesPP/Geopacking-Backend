package com.backend.geopacking.service;

import com.backend.geopacking.dto.MolinoDTO;
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
    List<Maquina> getMolinosActivos();

    Extrusora createExtrusora(Extrusora extrusora);
    Termoformadora createTermoformadora(Termoformadora termoformadora);
    Molino createMolino(MolinoDTO molino);


    Extrusora updateExtrusora(Long id, Extrusora details);
    Termoformadora updateTermoformadora(Long id, Termoformadora details);
    Molino updateMolino(Long id, MolinoDTO details);


    void deleteMaquina(Long id);
}