package com.backend.geopacking.service;

import com.backend.geopacking.dto.AdminDTO;
import com.backend.geopacking.dto.UserCreateDTO;
import com.backend.geopacking.dto.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO registerAdmin(AdminDTO admin);
    UserDTO registerUser(UserCreateDTO dto);
    List<UserDTO> getAllAdmins();
    UserDTO updateUser(long id, UserCreateDTO dto);
    void deleteUser(long id);
    List<UserDTO> getUsersByRole(String roleName);
    UserDTO getUserById(long id);
}
