package com.backend.geopacking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    private String turno;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "maquina_id", nullable = false)
    private Maquina maquina;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operador_id", nullable = false)
    private User operador;
}