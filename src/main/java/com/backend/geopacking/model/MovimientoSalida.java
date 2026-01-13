package com.backend.geopacking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "movimiento_salida_pt")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoSalida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaRegistro;
    private String registradoPor;
    private String motivo;
    private String comentarios;

    @OneToMany(mappedBy = "movimiento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleMovimientoSalida> detalles;
}
