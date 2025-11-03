package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.OperadorDTO;
import com.backend.geopacking.dto.UserDTO;
import com.backend.geopacking.model.Operador;
import com.backend.geopacking.model.RoleE;
import com.backend.geopacking.model.User;
import com.backend.geopacking.repository.OperadorRepository;
import com.backend.geopacking.repository.RoleRepository;
import com.backend.geopacking.service.OperadorService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Inyecta las dependencias (final)
public class OperadorServiceImpl implements OperadorService {

    private final OperadorRepository operadorRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO registerOperador(OperadorDTO operador) {
        if(operadorRepository.findByDni(operador.getDni()).isPresent()){
            throw new EntityExistsException("Operador con este DNI ya existe");
        };

        RoleE operadorRole = roleRepository.findByName("OPERATOR")
                .orElseThrow(() -> new RuntimeException("Rol OPERADOR no existe en la base de datos"));

        Operador user = Operador.builder()
                .dni(operador.getDni())
                .password(passwordEncoder.encode(operador.getPassword()))
                .name(operador.getName())
                .lastName(operador.getLastName())
                .sex(operador.getSex())
                .role(operadorRole)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        return mapToDTO(operadorRepository.save(user));
    }

    @Override
    public List<UserDTO> getAllOperadores() {
        return operadorRepository.findOperadorByRoleName("OPERATOR").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getOperadorById(long id) {
        Operador operador = operadorRepository.findOperadorById(id)
                .orElseThrow(() -> new EntityNotFoundException("Operador no encontrado o no es un OPERADOR"));
        return mapToDTO(operador);
    }

    @Override
    public UserDTO updateOperador(long id, OperadorDTO operador) {
        Operador operadorFound = operadorRepository.findOperadorById(id)
                .orElseThrow(() -> new EntityNotFoundException("Operador no encontrado o no es un OPERADOR"));


        if(operador.getDni() != null){
            operadorFound.setDni(operador.getDni());
        }
        if(operador.getPassword() != null){
            operadorFound.setPassword(passwordEncoder.encode(operador.getPassword()));
        }
        if(operador.getName() != null){
            operadorFound.setName(operador.getName());
        }
        if(operador.getLastName() != null){
            operadorFound.setLastName(operador.getLastName());
        }
        if(operador.getSex() != null){
            operadorFound.setSex(operador.getSex());
        }

        operadorFound.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return mapToDTO(operadorRepository.save(operadorFound));
    }

    @Override
    public void deleteOperador(long id) {
        Operador operadorFound = operadorRepository.findOperadorById(id)
                .orElseThrow(() -> new EntityNotFoundException("Operador no encontrado o no es un OPERADOR"));
        operadorRepository.delete(operadorFound);
    }


    private UserDTO mapToDTO(User user){
        return UserDTO.builder()
                .id(user.getId())
                .dni(user.getDni())
                .name(user.getName()) // Añadido
                .lastName(user.getLastName()) // Añadido
                .role(user.getRole().getName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
