package com.example.football.sesiones.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataSesionRepository extends JpaRepository<SesionJpaEntity, UUID> {
    Optional<SesionJpaEntity> findByTokenHash(String tokenHash);
}
