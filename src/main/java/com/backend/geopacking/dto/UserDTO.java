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

public class UserDTO {
    private long id;
    private String dni;
    private String name;
    private String lastName;
    private String role;
    private String sex;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
