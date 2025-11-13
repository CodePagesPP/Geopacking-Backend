package com.backend.geopacking.repository;

import com.backend.geopacking.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);
    @Query("SELECT c FROM Cliente c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
            "c.numeroDocumento LIKE CONCAT('%', :filtro, '%')")
    Page<Cliente> findByFiltro(@Param("filtro") String filtro, Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
            "c.numeroDocumento LIKE CONCAT('%', :filtro, '%')")
    List<Cliente> findByFiltro(@Param("filtro") String filtro, Sort sort);
}
