package com.backend.geopacking.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "registros_scrapp")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scrapp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long numeroBolson;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false)
    private Double pesoBruto;

    @Column(nullable = false)
    private Double pesoNeto;

    @Column(nullable = false)
    private LocalDate fechaCreacion;

    @Column(nullable = false)
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horaCreacion;

    @Column(nullable = false)
    private String turno;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "maquina_id", nullable = false)
    private Maquina maquina;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operador_id", nullable = false)
    private User operador;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_scrapp_id", nullable = false)
    private TypeScrapp typeScrapp;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "origen_id", nullable = true)
    private Origen origen;

    @Column(length = 500)
    private String observaciones;
}