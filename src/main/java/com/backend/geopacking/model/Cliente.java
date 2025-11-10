package com.backend.geopacking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "clientes", indexes = {
        @Index(name = "idx_cliente_num_doc", columnList = "numeroDocumento", unique = true)
})
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipoCliente;

    @Column(nullable = false)
    private String tipoDocumento;

    @Column(nullable = false, unique = true)
    private String numeroDocumento;

    @Column(nullable = false)
    private String nombre;

    private String nombreComercial;

    @Column(columnDefinition = "varchar(255) default 'PERU'")
    private String pais;
    private String departamento;
    private String provincia;
    private String distrito;
    private String direccion;

    private String telefono;

    @Email
    private String email;

    private LocalDate fechaNacimiento;

    private boolean creditoHabilitado;

    @Column(precision = 10, scale = 2)
    private BigDecimal montoCredito;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    private String contactoNombre; // Nombre del contacto
    private String contactoTelefono; // Teléfono del contacto
}
