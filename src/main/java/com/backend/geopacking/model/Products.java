package com.backend.geopacking.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "codigo_interno", nullable = false, unique = true)
    private String code;


    @Column(name = "referencia")
    private String referencia;

    @Column(name = "marca")
    private String marca;

    @Column(name = "linea")
    private String linea;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "unidad_medida", nullable = false)
    private String unidadDeMedida;



    @Column(name = "estado_activo", nullable = false)
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "material_id")
    private Material material;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "color_id")
    private Color color;
}
