package com.backend.geopacking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClienteDTO {

    @NotEmpty(message = "El tipo de cliente no puede estar vacío")
    private String tipoCliente;

    @NotEmpty(message = "El tipo de documento no puede estar vacío")
    private String tipoDocumento;

    @NotEmpty(message = "El número de documento no puede estar vacío")
    @Size(min = 8, max = 11, message = "El documento debe tener entre 8 y 11 caracteres")
    private String numeroDocumento;

    @NotEmpty(message = "El nombre no puede estar vacío")
    private String nombre;

    private String nombreComercial;
    private String pais;
    private String departamento;
    private String provincia;
    private String distrito;
    private String direccion;
    private String telefono;
    private String email;
    private LocalDate fechaNacimiento;
    private boolean creditoHabilitado;
    private BigDecimal montoCredito;
    private String observacion;
    private String contactoNombre;
    private String contactoTelefono;
}
