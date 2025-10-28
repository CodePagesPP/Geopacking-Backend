package com.backend.geopacking.auth;

import lombok.Data;

@Data
public class AuthRequest {
    private String dni;
    private String password;
}
