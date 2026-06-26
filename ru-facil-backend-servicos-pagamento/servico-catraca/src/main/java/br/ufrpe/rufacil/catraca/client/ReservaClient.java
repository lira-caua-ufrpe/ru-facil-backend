package br.ufrpe.rufacil.catraca.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "SERVICO-RESERVA")
public interface ReservaClient {

    @GetMapping("/reservas/validar/{cpf}/{turno}")
    Map<String, Object> validar(@PathVariable String cpf,
                                @PathVariable String turno);
}
