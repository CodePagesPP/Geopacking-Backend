package com.backend.geopacking.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "detalle_movimiento_salida_pt")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleMovimientoSalida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movimiento_id")
    @JsonIgnore
    private MovimientoSalida movimiento;

    private String codigoProducto;
    private String nombreProducto;
    private String loteProduccion;
    private Integer cantidad;
    private LocalDateTime fechaProduccion;
}
