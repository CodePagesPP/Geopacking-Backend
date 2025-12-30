package com.backend.geopacking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "scrapp_ex") // Esta es la tabla que guardará el registro del turno
public class ScrappEX {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;
    private Double cantidad;

    private Long typeScrappOriginalId;

    @ManyToOne
    @JoinColumn(name = "turno_id")
    @JsonIgnore // Para evitar bucles infinitos al serializar
    private TurnoEX turno;
}