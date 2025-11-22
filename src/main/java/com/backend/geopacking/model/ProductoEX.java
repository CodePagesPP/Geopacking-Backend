package com.backend.geopacking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
@Entity
@Table(name = "Producto_EX")
public class ProductoEX extends Products{

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "material_id")
    Material material;
}
