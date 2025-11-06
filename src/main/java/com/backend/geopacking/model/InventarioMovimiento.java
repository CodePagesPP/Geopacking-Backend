package com.backend.geopacking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_movimientos")
@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class InventarioMovimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigoMovimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigenMovimiento origen;

    @Column(nullable = false)
    private Double cantidad; // Siempre positivo, el tipo define si suma o resta

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrap_id")
    private Scrapp scrappReferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private User registradoPor;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.fecha == null) {
            this.fecha = LocalDate.now();
        }
    }
}
