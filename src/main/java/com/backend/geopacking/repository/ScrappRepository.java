package com.backend.geopacking.repository;

import com.backend.geopacking.model.Scrapp;
import com.backend.geopacking.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScrappRepository extends JpaRepository<Scrapp, Long> {
    Optional<Scrapp> findFirstByAnioOrderByNumeroBolsonDesc(int anio);
    @Query("SELECT COALESCE(SUM(s.pesoBruto), 0) FROM Scrapp s")
    Double sumPesoBruto();

    @Query("SELECT COALESCE(SUM(s.pesoNeto), 0) FROM Scrapp s")
    Double sumPesoNeto();

    Page<Scrapp> findAllByFechaCreacionBetween(LocalDate inicio, LocalDate fin, Pageable pageable);


    @Query("SELECT COALESCE(SUM(s.pesoBruto), 0) FROM Scrapp s WHERE s.fechaCreacion BETWEEN :inicio AND :fin")
    Double sumPesoBrutoBetween(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(s.pesoNeto), 0) FROM Scrapp s WHERE s.fechaCreacion BETWEEN :inicio AND :fin")
    Double sumPesoNetoBetween(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);


    List<Scrapp> findAllByFechaCreacionBetween(LocalDate inicio, LocalDate fin, Sort sort);

    Page<Scrapp> findAllByOperador(User operador, Pageable pageable);


    Page<Scrapp> findAllByOperadorAndFechaCreacionBetween(User operador, LocalDate inicio, LocalDate fin, Pageable pageable);


    @Query("SELECT COALESCE(SUM(s.pesoBruto), 0) FROM Scrapp s WHERE s.operador = :operador")
    Double sumPesoBrutoByOperador(@Param("operador") User operador);

    @Query("SELECT COALESCE(SUM(s.pesoNeto), 0) FROM Scrapp s WHERE s.operador = :operador")
    Double sumPesoNetoByOperador(@Param("operador") User operador);


    @Query("SELECT COALESCE(SUM(s.pesoBruto), 0) FROM Scrapp s WHERE s.operador = :operador AND s.fechaCreacion BETWEEN :inicio AND :fin")
    Double sumPesoBrutoByOperadorBetween(@Param("operador") User operador, @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(s.pesoNeto), 0) FROM Scrapp s WHERE s.operador = :operador AND s.fechaCreacion BETWEEN :inicio AND :fin")
    Double sumPesoNetoByOperadorBetween(@Param("operador") User operador, @Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}
