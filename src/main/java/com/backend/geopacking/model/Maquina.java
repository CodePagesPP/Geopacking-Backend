package com.backend.geopacking.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "maquinas")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@SuperBuilder

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "tipo"
)

@JsonSubTypes({
        @JsonSubTypes.Type(value = Extrusora.class, name = "Extrusora"),
        @JsonSubTypes.Type(value = Termoformadora.class, name = "Termoformadora"),
        @JsonSubTypes.Type(value = Molino.class, name = "Molino")
})
public abstract class Maquina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private boolean activo;
}