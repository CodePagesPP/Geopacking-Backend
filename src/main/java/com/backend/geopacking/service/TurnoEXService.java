package com.backend.geopacking.service;

import com.backend.geopacking.dto.RegistroTurnoDTO;
import com.backend.geopacking.model.TurnoEX;

public interface TurnoEXService {
    public TurnoEX guardarTurno(RegistroTurnoDTO dto);
}
