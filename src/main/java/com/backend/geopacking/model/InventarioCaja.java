package com.backend.geopacking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_caja")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "detalle_produccion_id")
    private DetalleProduccionTF detalleProduccion;

    private String loteProduccion;
    private String nombreProducto;
    private Integer cantidad;
    private LocalDateTime fechaProduccion;


    private String estado;
}
