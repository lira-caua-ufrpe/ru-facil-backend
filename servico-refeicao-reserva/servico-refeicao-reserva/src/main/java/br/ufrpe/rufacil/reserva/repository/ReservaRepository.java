package br.ufrpe.rufacil.reserva.repository;

import br.ufrpe.rufacil.reserva.model.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, String> {
}