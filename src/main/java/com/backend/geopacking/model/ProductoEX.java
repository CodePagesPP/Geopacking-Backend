package com.backend.geopacking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
@Entity
@Table(name = "Producto_EX")
public class ProductoEX extends Products{
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "producto_ex_materiales",
            joinColumns = @JoinColumn(name = "producto_ex_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    private List<Material> materiales;
}
