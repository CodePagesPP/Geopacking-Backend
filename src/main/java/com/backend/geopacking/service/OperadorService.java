package com.backend.geopacking.service;

import com.backend.geopacking.dto.OperadorDTO;
import com.backend.geopacking.dto.UserDTO;

import java.util.List;

public interface OperadorService {
    UserDTO registerOperador(OperadorDTO operador);
    List<UserDTO> getAllOperadores();
    UserDTO getOperadorById(long id);
    UserDTO updateOperador(long id, OperadorDTO operador);
    void deleteOperador(long id);
}
