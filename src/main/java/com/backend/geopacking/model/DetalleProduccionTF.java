package com.backend.geopacking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "detalle_produccion_tf")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleProduccionTF {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_trabajo_id")
    private OrdenTrabajoTF ordenTrabajo;

    private String codigoBobina;
    private String loteBobina;
    private Double velocidad;

    private LocalTime horaInicio;
    private LocalTime horaFin;

    private Integer cajas;
    private Double rechazoKg;

    private LocalDate fechaRegistro;
    private String registradoPor;

    @Column(name = "peso_promedio")
    private Double pesoPromedio;

    @Column(name = "bobina_fin")
    private Boolean bobinaFin;
}
