package br.ufrpe.rufacil.reserva.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import br.ufrpe.rufacil.reserva.model.RequestReservaDTO;
import br.ufrpe.rufacil.reserva.model.ReservaEntity;
import br.ufrpe.rufacil.reserva.repository.ReservaRepository;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    @Autowired
    private ReservaRepository reservaRepository;

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
            // Lendo como Map genérico para extrair o valor bruto e evitar problemas com DTO
            Map<?, ?> alunoMap = restTemplate.getForObject(urlPagamentoService, Map.class);
            
            String idReserva = "RES-" + System.currentTimeMillis();
            String categoria = "REGULAR";
            String valorRefeicao = "3.00";
            
            if (alunoMap != null) {
                // Testa dinamicamente todos os campos textuais possíveis vindo do Pagamento
                String cat1 = (String) alunoMap.get("categoria");
                String cat2 = (String) alunoMap.get("categoriaIdentificada");
                if ("ISENTO".equalsIgnoreCase(cat1) || "ISENTO".equalsIgnoreCase(cat2)) {
                    categoria = "ISENTO";
                    valorRefeicao = "0.00";
                }
            }
            
            // Se for ISENTO, o status vira "PAGO" (ou "CONFIRMADA_AUTOMATICAMENTE", o que sua catraca checar)
            String status = "ISENTO".equals(categoria) ? "PAGO" : "AGUARDANDO_PAGAMENTO";
            String msg = "ISENTO".equals(categoria) ? "Reserva liberada sem custos! Bom almoço." : "Gere o pagamento Pix para liberar o QR Code de acesso.";

            ReservaEntity novaReserva = new ReservaEntity(idReserva, dados.getCpf(), dados.getTipoRefeicao(), categoria, valorRefeicao, status, msg);
            reservaRepository.save(novaReserva);
            
            Map<String, Object> respostaSuccess = new HashMap<>();
            respostaSuccess.put("reservaId", idReserva);
            respostaSuccess.put("cpfAluno", dados.getCpf());
            respostaSuccess.put("statusReserva", status);
            respostaSuccess.put("mensagem", msg);
            respostaSuccess.put("valorCobrado", valorRefeicao);
            
            return ResponseEntity.ok(respostaSuccess);

        } catch (ResourceAccessException e) {
            Map<String, Object> respostaErroServico = new HashMap<>();
            respostaErroServico.put("statusReserva", "FALHA_TEMPORARIA");
            respostaErroServico.put("erro", "O Microsserviço de Pagamento está indisponível.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(respostaErroServico);
        }
    }

    // ENDPOINT NOVO QUE A CATRACA VAI CHAMAR PARA FAZER A VALIDAÇÃO:
    @GetMapping("/validar/{cpf}/{turno}")
    public ResponseEntity<?> validarParaCatraca(@PathVariable String cpf, @PathVariable String turno) {
        // Encontra as reservas no banco H2 para o CPF do aluno
        List<ReservaEntity> todas = reservaRepository.findAll();
        
        // Procura se existe alguma reserva ativa para o CPF que esteja PAGO ou CONFIRMADA_AUTOMATICAMENTE
        for (ReservaEntity res : todas) {
            if (cpf.equals(res.getCpfAluno()) && 
               ("PAGO".equalsIgnoreCase(res.getStatusReserva()) || "CONFIRMADA_AUTOMATICAMENTE".equalsIgnoreCase(res.getStatusReserva()))) {
                
                return ResponseEntity.ok(Map.of("status", "PAGO", "mensagem", "Reserva confirmada."));
            }
        }
        
        return ResponseEntity.ok(Map.of("status", "PENDENTE", "mensagem", "Nenhuma reserva paga encontrada para este turno."));
    }

    @GetMapping
    public ResponseEntity<List<ReservaEntity>> listarTodasAsReservas() {
        return ResponseEntity.ok(reservaRepository.findAll());
    }
}