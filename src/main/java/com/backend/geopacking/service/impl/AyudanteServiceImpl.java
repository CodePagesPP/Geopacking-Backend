package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.AyudanteDTO;
import com.backend.geopacking.dto.UserDTO;
import com.backend.geopacking.model.Ayudante;
import com.backend.geopacking.model.RoleE;
import com.backend.geopacking.model.User;
import com.backend.geopacking.repository.AyudanteRepository;
import com.backend.geopacking.repository.RoleRepository;
import com.backend.geopacking.service.AyudanteService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AyudanteServiceImpl implements AyudanteService {
    private final AyudanteRepository ayudanteRepository;
    private final RoleRepository roleRepository; // Asumo que tienes este repo
    private final PasswordEncoder passwordEncoder; // Asumo que lo tienes configurado

    @Override
    public UserDTO registerReporte(AyudanteDTO reporte) {
        if(ayudanteRepository.findByDni(reporte.getDni()).isPresent()){
            throw new EntityExistsException("Reporte con este DNI ya existe");
        };

        // Buscamos el rol "Reporte"
        RoleE ReporteRole = roleRepository.findByName("REPORT")
                .orElseThrow(() -> new RuntimeException("Rol Reporte no existe en la base de datos"));

        Ayudante user = Ayudante.builder()
                .dni(reporte.getDni())
                .password(passwordEncoder.encode(reporte.getPassword()))
                .name(reporte.getName())
                .lastName(reporte.getLastName())
                .sex(reporte.getSex())
                .role(ReporteRole)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        return mapToDTO(ayudanteRepository.save(user));
    }

    @Override
    public List<UserDTO> getAllReportes() {
        return ayudanteRepository.findReporteByRoleName("REPORT").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getReporteById(long id) {
        Ayudante ayudante = ayudanteRepository.findReporteById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado o no es un Reporte"));
        return mapToDTO(ayudante);
    }

    @Override
    public UserDTO updateReporte(long id, AyudanteDTO Reporte) {
        Ayudante ayudanteFound = ayudanteRepository.findReporteById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado o no es un Reporte"));

        // Actualización selectiva (similar a tu updateAdmin)
        if(Reporte.getDni() != null){
            ayudanteFound.setDni(Reporte.getDni());
        }
        if(Reporte.getPassword() != null){
            ayudanteFound.setPassword(passwordEncoder.encode(Reporte.getPassword()));
        }
        if(Reporte.getName() != null){
            ayudanteFound.setName(Reporte.getName());
        }
        if(Reporte.getLastName() != null){
            ayudanteFound.setLastName(Reporte.getLastName());
        }
        if(Reporte.getSex() != null){
            ayudanteFound.setSex(Reporte.getSex());
        }

        ayudanteFound.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return mapToDTO(ayudanteRepository.save(ayudanteFound));
    }

    @Override
    public void deleteReporte(long id) {
        Ayudante ayudanteFound = ayudanteRepository.findReporteById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado o no es un Reporte"));
        ayudanteRepository.delete(ayudanteFound);
    }

    private UserDTO mapToDTO(User user){
        return UserDTO.builder()
                .id(user.getId())
                .dni(user.getDni())
                .name(user.getName())
                .lastName(user.getLastName())
                .role(user.getRole().getName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
