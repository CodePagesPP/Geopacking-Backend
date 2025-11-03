package com.backend.geopacking.repository;

import com.backend.geopacking.model.Scrapp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScrappRepository extends JpaRepository<Scrapp, Long> {
    Optional<Scrapp> findFirstByAnioOrderByNumeroBolsonDesc(int anio);
}
