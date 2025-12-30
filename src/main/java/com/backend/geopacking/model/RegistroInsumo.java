package com.backend.geopacking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_insumos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private LocalDate fecha;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.fecha == null) {
            this.fecha = LocalDate.now();
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Operacion operacion;

    @Column(nullable = false)
    private Double cantidad;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "motivo_id")
    private Motivo motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRegistro tipoRegistro;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = true)
    private User registradoPor;
}
