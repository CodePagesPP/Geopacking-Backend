package com.backend.geopacking.service.impl;

import com.backend.geopacking.dto.AdminDTO;
import com.backend.geopacking.dto.UserDTO;
import com.backend.geopacking.dto.UserResponseDTO;
import com.backend.geopacking.model.Admin;
import com.backend.geopacking.model.RoleE;
import com.backend.geopacking.model.User;
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
        System.out.println("ROL ENCONTRADO: " + adminRole.getName());


        Admin user = Admin.builder()
                .dni(admin.getDni())
                .password(passwordEncoder.encode(admin.getPassword()))
                .role(adminRole)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        System.out.println(user.getRole());
        return mapToDTO(adminRepository.save(user));
    }


    @Override
    public List<UserDTO> getAllAdmins() {
        return adminRepository.findAdminByRoleName("ADMIN").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateAdmin(long id, AdminDTO admin) {

        Admin adminFound = adminRepository.findAdminById(id)
                .orElseThrow(() -> new EntityNotFoundException("ADMIN not found or is not a ADMIN"));
        //Por si se quiere actualizar solo el dni o la password
        if(admin.getDni() != null){
            adminFound.setDni(admin.getDni());
        }

        if(admin.getPassword() != null){
            adminFound.setPassword(passwordEncoder.encode(admin.getPassword()));
        }

        adminFound.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return mapToDTO(adminRepository.save(adminFound));
    }

    @Override
    public void deleteAdmin(long id) {
        Admin adminFound = adminRepository.findAdminById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found or is not a ADMIN"));
        adminRepository.delete(adminFound);
    }



    private UserDTO mapToDTO(Admin user){
        return UserDTO.builder()
                .id(user.getId())
                .dni(user.getDni())
                .role(user.getRole().getName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private UserResponseDTO mapToDTO(User dto){
        return UserResponseDTO.builder()
                .id(dto.getId())
                .name(dto.getName())
                .lastName(dto.getLastName())
                .dni(dto.getDni())
                .sex(dto.getSex())
                .role(dto.getRole().getName())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
    }
}