package br.ufrpe.rufacil.reserva.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Cliente Feign para o servico-pagamento-faturamento.
 * Consulta dados do aluno no SIGAA e saldo de fichas.
 */
@FeignClient(name = "SERVICO-PAGAMENTO")
public interface FichaClient {

    @GetMapping("/pagamentos/sigaa/{cpf}")
    Map<String, Object> consultarAluno(@PathVariable String cpf);

    @GetMapping("/pagamentos/fichas/{cpf}")
    Map<String, Object> consultarFichas(@PathVariable String cpf);
}
