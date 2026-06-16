package br.ufrpe.rufacil.reserva.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import br.ufrpe.rufacil.reserva.model.RequestReservaDTO;
import br.ufrpe.rufacil.reserva.model.ResponseAlunoDTO;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    @PostMapping
    public ResponseEntity<Map<String, Object>> criarReserva(@RequestBody RequestReservaDTO dados) {
        
        if (dados.getCpf() == null || dados.getTipoRefeicao() == null) {
            Map<String, Object> erroBadRequest = new HashMap<>();
            erroBadRequest.put("erro", "Dados incompletos. CPF e Tipo de Refeição são obrigatórios.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erroBadRequest);
        }

        RestTemplate restTemplate = new RestTemplate();
        // Comunicação apontando para o Microsserviço de Pagamento na porta 8082
        String urlPagamentoService = "http://localhost:8082/api/v1/pagamentos/sigaa/" + dados.getCpf();
        
        try {
            // Consome o microsserviço de Pagamento mapeando a resposta para o DTO do Model
            ResponseAlunoDTO dadosAluno = restTemplate.getForObject(urlPagamentoService, ResponseAlunoDTO.class);
            
            Map<String, Object> respostaSuccess = new HashMap<>();
            respostaSuccess.put("reservaId", "RES-" + System.currentTimeMillis());
            respostaSuccess.put("cpfAluno", dados.getCpf());
            respostaSuccess.put("tipoRefeicao", dados.getTipoRefeicao());
            respostaSuccess.put("categoriaIdentificada", dadosAluno.getCategoria());
            respostaSuccess.put("valorCobrado", dadosAluno.getValorRefeicao());
            
            if ("ISENTO".equals(dadosAluno.getCategoria())) {
                respostaSuccess.put("statusReserva", "CONFIRMADA_AUTOMATICAMENTE");
                respostaSuccess.put("mensagem", "Reserva liberada sem custos! Bom almoço.");
            } else {
                respostaSuccess.put("statusReserva", "AGUARDANDO_PAGAMENTO");
                respostaSuccess.put("mensagem", "Gere o pagamento Pix para liberar o QR Code de acesso.");
            }
            
            return ResponseEntity.ok(respostaSuccess);

        } catch (ResourceAccessException e) {
            // Resiliência de arquitetura se o serviço de pagamento estiver fora do ar
            Map<String, Object> respostaErroServico = new HashMap<>();
            respostaErroServico.put("statusReserva", "FALHA_TEMPORARIA");
            respostaErroServico.put("erro", "Não foi possível se communicate com o serviço de Pagamentos.");
            respostaErroServico.put("detalhes", "O Microsserviço de Pagamento/Faturamento está temporariamente indisponível.");
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(respostaErroServico);
        }
    }
}