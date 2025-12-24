package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.AdminDTO;
import com.backend.geopacking.dto.UserCreateDTO;
import com.backend.geopacking.dto.UserDTO;
import com.backend.geopacking.dto.UserResponseDTO;
import com.backend.geopacking.model.*;
import com.backend.geopacking.repository.AdminRepository;
import com.backend.geopacking.repository.RoleRepository;
import com.backend.geopacking.repository.UserRepository;
import com.backend.geopacking.service.UserService;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final AdminRepository adminRepository;
    private final UserRepository userRepository2;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserDTO registerAdmin(AdminDTO admin) {
        if(adminRepository.findByDni(admin.getDni()).isPresent()){
            throw new EntityExistsException("User with this dni already exists");
        };

        RoleE adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Rol ADMIN no existe en la base de datos"));


        Admin user = Admin.builder()
                .dni(admin.getDni())
                .password(passwordEncoder.encode(admin.getPassword()))
                .role(adminRole)
                .name(admin.getName())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        System.out.println(user.getRole());
        return mapToDTO(adminRepository.save(user));
    }


    @Override
    public UserDTO registerUser(UserCreateDTO dto) {

        if(userRepository2.findByDni(dto.getDni()).isPresent()){
            throw new EntityExistsException("El usuario con este DNI ya existe");
        }


        RoleE roleEntity = roleRepository.findByName(dto.getRole())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + dto.getRole()));


        User userToSave;

        switch (dto.getRole()) {
            case "ADMIN":

                userToSave = Admin.builder().build();
                break;

            case "OPERATOR":
                userToSave = Operador.builder().build();
                break;

            case "AYUDANTE":
                userToSave = Ayudante.builder().build();
                break;

            default:
                throw new IllegalArgumentException("Tipo de rol no soportado para registro");
        }


        userToSave.setDni(dto.getDni());
        userToSave.setName(dto.getName());
        userToSave.setLastName(dto.getLastName());
        userToSave.setSex(dto.getSex());
        userToSave.setPassword(passwordEncoder.encode(dto.getPassword()));
        userToSave.setRole(roleEntity);
        userToSave.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));


        User savedUser = userRepository2.save(userToSave);

        return mapToDTO(savedUser);
    }


    @Override
    public List<UserDTO> getAllAdmins() {
        return adminRepository.findAdminByRoleName("ADMIN").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getUsersByRole(String roleName) {

        if (roleName == null || roleName.equals("ALL")) {
            return userRepository2.findAll().stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        // Busca por el rol específico ("ADMIN", "OPERADOR", etc.)
        return userRepository2.findByRole_Name(roleName).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(long id, UserCreateDTO dto) {

        User userFound = userRepository2.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + id));

        if (dto.getDni() != null && !dto.getDni().isEmpty()) {
            if (!userFound.getDni().equals(dto.getDni())) {
                if (userRepository2.findByDni(dto.getDni()).isPresent()) {
                    throw new EntityExistsException("Ya existe otro usuario con el DNI: " + dto.getDni());
                }
                userFound.setDni(dto.getDni());
            }
        }


        if (dto.getName() != null) userFound.setName(dto.getName());
        if (dto.getLastName() != null) userFound.setLastName(dto.getLastName());
        if (dto.getSex() != null) userFound.setSex(dto.getSex());


        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            userFound.setPassword(passwordEncoder.encode(dto.getPassword()));
        }


        userFound.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return mapToDTO(userRepository2.save(userFound));
    }

    @Override
    public void deleteUser(long id) {
        if (!userRepository2.existsById(id)) {
            throw new EntityNotFoundException("Usuario no encontrado con ID: " + id);
        }
        userRepository2.deleteById(id);
    }

    @Override
    public UserDTO getUserById(long id) {
        User user = userRepository2.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con el ID: " + id));

        return mapToDTO(user);
    }

    private UserDTO mapToDTO(User user){
        return UserDTO.builder()
                .id(user.getId())
                .dni(user.getDni())
                .name(user.getName())
                .lastName(user.getLastName())
                .sex(user.getSex())
                .role(user.getRole().getName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}