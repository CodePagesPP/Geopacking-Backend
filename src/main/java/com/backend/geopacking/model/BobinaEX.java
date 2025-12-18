package com.backend.geopacking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "bobina_ex")
public class BobinaEX {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private Double pesoBruto;
    private Double pesoNeto;

    @ManyToOne
    @JoinColumn(name = "turno_id")
    @JsonIgnore
    private TurnoEX turno;
}
