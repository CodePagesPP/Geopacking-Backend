package com.backend.geopacking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AyudanteDTO {
    private String dni;
    private String password;
    private String name;
    private String lastName;
    private String sex;
}
