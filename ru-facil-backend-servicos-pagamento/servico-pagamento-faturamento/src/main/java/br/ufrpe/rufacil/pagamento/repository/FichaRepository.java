package br.ufrpe.rufacil.pagamento.repository;

import br.ufrpe.rufacil.pagamento.model.FichaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FichaRepository extends JpaRepository<FichaEntity, String> {
}
