package com.backend.geopacking.model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Entity
@Table(name = "turno_ex")
public class TurnoEX {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHoraFin;


    private Double totalKilosProducidos;
    private Integer cantidadBobinas;

    @ManyToOne
    @JoinColumn(name = "ot_id")
    private OrdenTrabajoEX ordenTrabajo;

    @OneToMany(mappedBy = "turno", cascade = CascadeType.ALL)
    private List<BobinaEX> bobinas;

    @OneToMany(mappedBy = "turno", cascade = CascadeType.ALL)
    private List<MaterialEX> materiales;
    @OneToMany(mappedBy = "turno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScrappEX> scrapps;
    private String comentarios;
    private String usuarioNombre;
}
