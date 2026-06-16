package br.ufrpe.rufacil.reserva.controller;

import br.ufrpe.rufacil.reserva.model.RequestReservaDTO;
import br.ufrpe.rufacil.reserva.model.ResponseAlunoDTO;
import br.ufrpe.rufacil.reserva.model.ReservaEntity;
import br.ufrpe.rufacil.reserva.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    @Autowired
    private ReservaRepository reservaRepository;

    // Método POST original atualizado salvando no Banco de Dados
    @PostMapping
    public ResponseEntity<Map<String, Object>> criarReserva(@RequestBody RequestReservaDTO dados) {
        if (dados.getCpf() == null || dados.getTipoRefeicao() == null) {
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "CPF e Tipo de Refeição são obrigatórios.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
        }

        RestTemplate restTemplate = new RestTemplate();
        String urlPagamentoService = "http://localhost:8082/api/v1/pagamentos/sigaa/" + dados.getCpf();
        
        try {
            ResponseAlunoDTO dadosAluno = restTemplate.getForObject(urlPagamentoService, ResponseAlunoDTO.class);
            
            String idReserva = "RES-" + System.currentTimeMillis();
            String status = "ISENTO".equals(dadosAluno.getCategoria()) ? "CONFIRMADA_AUTOMATICAMENTE" : "AGUARDANDO_PAGAMENTO";
            String msg = "ISENTO".equals(dadosAluno.getCategoria()) ? "Reserva liberada sem custos! Bom almoço." : "Gere o pagamento Pix para liberar o QR Code de acesso.";

            // PERSISTÊNCIA REAL AQUI: Salvando a reserva no banco de dados H2
            ReservaEntity novaReserva = new ReservaEntity(idReserva, dados.getCpf(), dados.getTipoRefeicao(), dadosAluno.getCategoria(), dadosAluno.getValorRefeicao(), status, msg);
            reservaRepository.save(novaReserva);
            
            Map<String, Object> respostaSuccess = new HashMap<>();
            respostaSuccess.put("reservaId", idReserva);
            respostaSuccess.put("cpfAluno", dados.getCpf());
            respostaSuccess.put("statusReserva", status);
            respostaSuccess.put("mensagem", msg);
            respostaSuccess.put("valorCobrado", dadosAluno.getValorRefeicao());
            
            return ResponseEntity.ok(respostaSuccess);

        } catch (ResourceAccessException e) {
            Map<String, Object> respostaErroServico = new HashMap<>();
            respostaErroServico.put("statusReserva", "FALHA_TEMPORARIA");
            respostaErroServico.put("erro", "O Microsserviço de Pagamento está indisponível.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(respostaErroServico);
        }
    }

    // NOVO ENDPOINT RESTFUL PARA LISTAR DO BANCO: Provando o controle de dados pro professor
    @GetMapping
    public ResponseEntity<List<ReservaEntity>> listarTodasAsReservas() {
        return ResponseEntity.ok(reservaRepository.findAll());
    }
}