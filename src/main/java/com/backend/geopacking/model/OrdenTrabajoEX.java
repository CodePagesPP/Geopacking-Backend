package com.backend.geopacking.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import static com.backend.geopacking.model.EstadoOT_EX.EN_ESPERA;

@Data
@Entity
@Table(name = "orden_trabajo_ex")
public class OrdenTrabajoEX {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "maquina_id", nullable = false)
    private Maquina maquina;

    @ManyToOne
    @JoinColumn(name = "producto_ex_id", nullable = false)
    private ProductoEX producto;

    private Double requerimientoKg;

    private Double producidoKg;

    @Enumerated(EnumType.STRING)
    private EstadoOT_EX estado;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User creadaPor;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.producidoKg = 0.0;
        this.estado = EN_ESPERA;
    }
}
