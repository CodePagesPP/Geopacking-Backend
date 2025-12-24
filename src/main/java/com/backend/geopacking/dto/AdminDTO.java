package com.backend.geopacking.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor

public class AdminDTO {
    private String dni;
    private String password;
    private String role;
    private String name;
}
