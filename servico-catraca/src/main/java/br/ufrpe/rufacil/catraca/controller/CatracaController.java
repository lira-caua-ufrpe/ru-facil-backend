package br.ufrpe.rufacil.catraca.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import br.ufrpe.rufacil.catraca.model.RequisicaoCatracaDTO;

@RestController
@RequestMapping("/catraca")
public class CatracaController {

    // Substituímos o "new RestTemplate()" pelo @Autowired para usar o Bean balanceado pelo Eureka
    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/passar-carteirinha")
    public ResponseEntity<?> validarAcesso(@RequestBody RequisicaoCatracaDTO requisicao) {
        // MUDANÇA AQUI: Trocamos "localhost:8081" pelo ID do serviço no Eureka "servico-reserva"
        String urlReserva = "http://servico-reserva/reservas/validar/" + requisicao.getCpf() + "/" + requisicao.getTurno();

        try {
            Map<?, ?> resposta = restTemplate.getForObject(urlReserva, Map.class);
            
            if (resposta != null && "PAGO".equals(resposta.get("status"))) {
                Map<String, String> sucesso = new HashMap<>();
                sucesso.put("status", "LIBERADO");
                sucesso.put("mensagem", "Acesso autorizado! Bom apetite.");
                return ResponseEntity.ok(sucesso);
            }
            
            Map<String, String> bloqueado = new HashMap<>();
            bloqueado.put("status", "BLOQUEADO");
            bloqueado.put("mensagem", "Nenhuma reserva paga encontrada.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(bloqueado);

        } catch (Exception e) {
            Map<String, String> erro = new HashMap<>();
            erro.put("status", "ERRO");
            erro.put("mensagem", "Erro ao comunicar com o serviço de reservas.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }
}