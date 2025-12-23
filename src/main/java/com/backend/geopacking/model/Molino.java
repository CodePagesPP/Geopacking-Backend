package com.backend.geopacking.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "molinos")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "origenes")
@ToString(callSuper = true, exclude = "origenes")
@NoArgsConstructor
@SuperBuilder
public class Molino extends Maquina {
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "molino_origenes",
            joinColumns = @JoinColumn(name = "molino_id"),
            inverseJoinColumns = @JoinColumn(name = "origen_id")
    )
    @Builder.Default
    @JsonManagedReference
    private Set<Origen> origenes = new HashSet<>();
}