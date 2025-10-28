package com.backend.geopacking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name="admin")
@Data
@NoArgsConstructor
@SuperBuilder
public class Admin extends User{
}
