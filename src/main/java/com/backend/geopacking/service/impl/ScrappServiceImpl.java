package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.ScrappDTO;
import com.backend.geopacking.model.Maquina;
import com.backend.geopacking.model.Scrapp;
import com.backend.geopacking.model.User;
import com.backend.geopacking.repository.MaquinaRepository;
import com.backend.geopacking.repository.ScrappRepository;
import com.backend.geopacking.repository.UserRepository;
import com.backend.geopacking.service.ScrappService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScrappServiceImpl implements ScrappService {
    private final ScrappRepository registroScrappRepository;
    private final MaquinaRepository maquinaRepository;
    private final UserRepository userRepository;

    @Override
    public Scrapp registrarScrapp(ScrappDTO dto, UserDetails userDetails) {

        // 1. Obtener entidades relacionadas
        User user = userRepository.findByDni(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Maquina maquina = maquinaRepository.findById(dto.getMaquinaId())
                .orElseThrow(() -> new EntityNotFoundException("Máquina no encontrada"));

        // 2. Lógica del Contador de Bolsón
        int anioActual = LocalDate.now().getYear();

        // Buscamos el último bolsón registrado ESTE AÑO
        Optional<Scrapp> ultimoRegistro = registroScrappRepository
                .findFirstByAnioOrderByNumeroBolsonDesc(anioActual);

        // Si existe, tomamos su número y le sumamos 1. Si no, empezamos en 1.
        long nuevoNumeroBolson = ultimoRegistro.isPresent()
                ? ultimoRegistro.get().getNumeroBolson() + 1
                : 1;

        // 3. Crear la nueva entidad
        Scrapp nuevoRegistro = Scrapp.builder()
                .numeroBolson(nuevoNumeroBolson)
                .anio(anioActual)
                .pesoBruto(dto.getPesoBruto())
                .pesoNeto(dto.getPesoNeto())
                .fechaCreacion(LocalDate.now())
                .turno(obtenerTurnoActual())
                .maquina(maquina)
                .operador(user)
                .build();

        // 4. Guardar y devolver
        return registroScrappRepository.save(nuevoRegistro);
    }

    @Override
    public List<Scrapp> obtenerTodos() {
        return registroScrappRepository.findAll();
    }

    @Override
    public Scrapp obtenerPorId(Long id) {
        return registroScrappRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Registro de Scrapp no encontrado"));
    }

    private String obtenerTurnoActual() {
        int hora = LocalTime.now().getHour();
        if (hora < 14) return "M";
        else if (hora < 22) return "T";
        else return "N";
    }
}
