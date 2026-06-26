package br.ufrpe.rufacil.pagamento.repository;

import br.ufrpe.rufacil.pagamento.model.PagamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagamentoRepository extends JpaRepository<PagamentoEntity, String> {
}