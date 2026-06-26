package com.example.servico_acesso.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.servico_acesso.client.PagamentoClient;
import com.example.servico_acesso.client.ReservaClient;

@RestController
@RequestMapping("/acesso")
public class AcessoController {

    @Autowired
    private ReservaClient reservaClient;

    @Autowired
    private PagamentoClient pagamentoClient;

    @PostMapping("/validar")
    public ResponseEntity<?> validar(@RequestBody Map<String, String> req) {

        String cpf = req.get("cpf");
        String turno = req.get("turno");

        Map<String, Object> reserva = reservaClient.validar(cpf, turno);

        if ("PAGO".equals(reserva.get("status"))) {
            return ResponseEntity.ok(Map.of(
                "status", "LIBERADO",
                "origem", "RESERVA"
            ));
        }

        Map<String, Object> pagamento = pagamentoClient.buscar(cpf);

        if ("ISENTO".equals(pagamento.get("categoria"))) {
            return ResponseEntity.ok(Map.of(
                "status", "LIBERADO",
                "origem", "PAGAMENTO"
            ));
        }

        return ResponseEntity.status(403).body(Map.of(
            "status", "BLOQUEADO"
        ));
    }
}