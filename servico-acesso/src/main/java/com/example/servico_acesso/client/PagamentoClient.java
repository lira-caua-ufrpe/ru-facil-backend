package com.example.servico_acesso.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


    @FeignClient(name = "SERVICO-PAGAMENTO")
public interface PagamentoClient {

    @GetMapping("/pagamentos/sigaa/{cpf}")
    Map<String, Object> buscar(@PathVariable String cpf);
}

