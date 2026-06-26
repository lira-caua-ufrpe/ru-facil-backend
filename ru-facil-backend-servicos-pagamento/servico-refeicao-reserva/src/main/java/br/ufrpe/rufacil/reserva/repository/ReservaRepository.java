package br.ufrpe.rufacil.reserva.repository;

import br.ufrpe.rufacil.reserva.model.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<ReservaEntity, String> {

    List<ReservaEntity> findByCpfAlunoAndStatusReserva(String cpfAluno, String statusReserva);

    List<ReservaEntity> findByCpfAluno(String cpfAluno);
}
