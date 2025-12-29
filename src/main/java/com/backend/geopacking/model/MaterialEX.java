package com.backend.geopacking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "material_ex")
public class MaterialEX {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private Double cantidadKg;

    private Long materialOriginalId;

    @ManyToOne
    @JoinColumn(name = "turno_id")
    @JsonIgnore
    private TurnoEX turno;
}
