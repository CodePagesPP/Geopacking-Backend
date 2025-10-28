package com.backend.geopacking.service;

import com.backend.geopacking.dto.AdminDTO;
import com.backend.geopacking.dto.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO registerAdmin(AdminDTO admin);
    List<UserDTO> getAllAdmins();
    UserDTO updateAdmin(long id, AdminDTO admin);
    void deleteAdmin(long id);
}
