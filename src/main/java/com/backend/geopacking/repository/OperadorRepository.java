package com.backend.geopacking.repository;

import com.backend.geopacking.model.Operador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperadorRepository extends JpaRepository<Operador, Long> {

    Optional<Operador> findByDni(String dni);

    @Query("SELECT o FROM Operador o WHERE o.role.name = :roleName")
    List<Operador> findOperadorByRoleName(@Param("roleName") String roleName);

    @Query("SELECT o FROM Operador o WHERE o.id = :id AND o.role.name = 'OPERATOR'")
    Optional<Operador> findOperadorById(@Param("id") long id);
}
