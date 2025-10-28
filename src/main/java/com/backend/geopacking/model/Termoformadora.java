package com.backend.geopacking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "termoformadoras")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Termoformadora extends Maquina {

    @Column(name = "area_formado", nullable = false)
    private String areaDeFormado;
}