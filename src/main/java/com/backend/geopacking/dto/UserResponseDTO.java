package com.backend.geopacking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private long id;
    private String role;
    private String name;
    private String lastName;
    private String sex;
    private String dni;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
