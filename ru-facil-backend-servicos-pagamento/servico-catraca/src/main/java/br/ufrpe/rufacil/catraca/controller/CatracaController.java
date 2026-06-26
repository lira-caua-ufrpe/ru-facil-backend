package br.ufrpe.rufacil.catraca.controller;

import br.ufrpe.rufacil.catraca.client.FichaDescontoClient;
import br.ufrpe.rufacil.catraca.client.ReservaClient;
import br.ufrpe.rufacil.catraca.model.RequisicaoCatracaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/catraca")
public class CatracaController {

    @Autowired private ReservaClient reservaClient;
    @Autowired private FichaDescontoClient fichaDescontoClient;

    /**
     * Valida a passagem do aluno na catraca.
     *
     * Fluxo unificado:
     *   1. Chama /reservas/validar/{cpf}/{turno}
     *      - Se ISENTO → retorna LIBERADO direto (sem descontar ficha)
     *      - Se LIBERADO (reserva confirmada) → desconta 1 ficha no servico-pagamento
     *      - Se BLOQUEADO → nega acesso
     *   2. Retorna resultado com mensagem clara
     *
     * Body: { "cpf": "222", "turno": "ALMOCO" }
     */
    @PostMapping("/passar")
    public ResponseEntity<?> passarNaCatraca(@RequestBody RequisicaoCatracaDTO requisicao) {
        if (requisicao.getCpf() == null || requisicao.getTurno() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "CPF e turno são obrigatórios."));
        }

        // Passo 1: valida reserva/categoria no servico-reserva
        Map<String, Object> validacao;
        try {
            validacao = reservaClient.validar(requisicao.getCpf(), requisicao.getTurno());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("erro", "Serviço de reserva indisponível: " + e.getMessage()));
        }

        String status   = (String) validacao.getOrDefault("status", "BLOQUEADO");
        String categoria = (String) validacao.getOrDefault("categoria", "");
        String mensagem = (String) validacao.getOrDefault("mensagem", "");

        if (!"LIBERADO".equals(status)) {
            return ResponseEntity.ok(Map.of(
                    "acesso",   "NEGADO",
                    "cpf",      requisicao.getCpf(),
                    "turno",    requisicao.getTurno(),
                    "mensagem", mensagem
            ));
        }

        // Passo 2: isento não desconta ficha
        if ("ISENTO".equals(categoria)) {
            return ResponseEntity.ok(Map.of(
                    "acesso",   "LIBERADO",
                    "cpf",      requisicao.getCpf(),
                    "turno",    requisicao.getTurno(),
                    "categoria","ISENTO",
                    "mensagem", "Catraca liberada! Acesso sem custo. Bom apetite!"
            ));
        }

        // Passo 3: desconta uma ficha do saldo do aluno
        int fichasRestantes = 0;
        try {
            Map<String, Object> desconto = fichaDescontoClient.descontarFicha(requisicao.getCpf());
            Object saldo = desconto.get("fichasRestantes");
            if (saldo != null) fichasRestantes = Integer.parseInt(saldo.toString());
        } catch (Exception e) {
            // Se não conseguir descontar, bloqueia para não liberar sem débito
            return ResponseEntity.status(503).body(Map.of(
                    "acesso",   "NEGADO",
                    "mensagem", "Erro ao descontar ficha. Procure o atendimento do RU.",
                    "detalhe",  e.getMessage()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "acesso",           "LIBERADO",
                "cpf",              requisicao.getCpf(),
                "turno",            requisicao.getTurno(),
                "fichasRestantes",  fichasRestantes,
                "mensagem",         String.format(
                        "Catraca liberada! Ficha descontada. Saldo restante: %d ficha(s). Bom apetite!",
                        fichasRestantes)
        ));
    }
}
