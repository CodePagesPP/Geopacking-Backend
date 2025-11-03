package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.ReporteDTO;
import com.backend.geopacking.dto.UserDTO;
import com.backend.geopacking.model.Reporte;
import com.backend.geopacking.model.RoleE;
import com.backend.geopacking.model.User;
import com.backend.geopacking.repository.ReporteRepository;
import com.backend.geopacking.repository.RoleRepository;
import com.backend.geopacking.service.ReporteService;
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
public class ReporteServiceImpl implements ReporteService {
    private final ReporteRepository reporteRepository;
    private final RoleRepository roleRepository; // Asumo que tienes este repo
    private final PasswordEncoder passwordEncoder; // Asumo que lo tienes configurado

    @Override
    public UserDTO registerReporte(ReporteDTO reporte) {
        if(reporteRepository.findByDni(reporte.getDni()).isPresent()){
            throw new EntityExistsException("Reporte con este DNI ya existe");
        };

        // Buscamos el rol "Reporte"
        RoleE ReporteRole = roleRepository.findByName("REPORT")
                .orElseThrow(() -> new RuntimeException("Rol Reporte no existe en la base de datos"));

        Reporte user = Reporte.builder()
                .dni(reporte.getDni())
                .password(passwordEncoder.encode(reporte.getPassword()))
                .name(reporte.getName())
                .lastName(reporte.getLastName())
                .sex(reporte.getSex())
                .role(ReporteRole)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        return mapToDTO(reporteRepository.save(user));
    }

    @Override
    public List<UserDTO> getAllReportes() {
        return reporteRepository.findReporteByRoleName("REPORT").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getReporteById(long id) {
        Reporte reporte = reporteRepository.findReporteById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado o no es un Reporte"));
        return mapToDTO(reporte);
    }

    @Override
    public UserDTO updateReporte(long id, ReporteDTO Reporte) {
        Reporte ReporteFound = reporteRepository.findReporteById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado o no es un Reporte"));

        // Actualización selectiva (similar a tu updateAdmin)
        if(Reporte.getDni() != null){
            ReporteFound.setDni(Reporte.getDni());
        }
        if(Reporte.getPassword() != null){
            ReporteFound.setPassword(passwordEncoder.encode(Reporte.getPassword()));
        }
        if(Reporte.getName() != null){
            ReporteFound.setName(Reporte.getName());
        }
        if(Reporte.getLastName() != null){
            ReporteFound.setLastName(Reporte.getLastName());
        }
        if(Reporte.getSex() != null){
            ReporteFound.setSex(Reporte.getSex());
        }

        ReporteFound.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return mapToDTO(reporteRepository.save(ReporteFound));
    }

    @Override
    public void deleteReporte(long id) {
        Reporte ReporteFound = reporteRepository.findReporteById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado o no es un Reporte"));
        reporteRepository.delete(ReporteFound);
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
