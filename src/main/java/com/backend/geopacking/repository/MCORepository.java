package com.backend.geopacking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.Optional;

@NoRepositoryBean
public interface MCORepository<T> extends JpaRepository<T,Long> {
    Optional<T> findByCode(String code);
    Optional<T> findByName(String name);
}
