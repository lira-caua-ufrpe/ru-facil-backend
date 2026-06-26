package br.ufrpe.rufacil.catraca.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

/**
 * Chama o servico-pagamento para descontar uma ficha após a validação da catraca.
 */
@FeignClient(name = "SERVICO-PAGAMENTO")
public interface FichaDescontoClient {

    @PostMapping("/pagamentos/fichas/descontar/{cpf}")
    Map<String, Object> descontarFicha(@PathVariable String cpf);
}
