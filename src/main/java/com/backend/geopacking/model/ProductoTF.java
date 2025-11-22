package com.backend.geopacking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
@Entity
@Table(name = "Producto_TF")
public class ProductoTF extends Products{

    @Column(name = "peso_unitario_g")
    private Double pesoUnitario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_base_id")
    private ProductoEX productoBase;
}
