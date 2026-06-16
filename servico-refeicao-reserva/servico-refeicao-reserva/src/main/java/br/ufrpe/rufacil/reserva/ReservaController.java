package br.ufrpe.rufacil.reserva;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    // ROTA PARA SOLICITAR UMA RESERVA DE REFEIÇÃO (UC02)
    @PostMapping
    public Map<String, Object> criarReserva(@RequestBody Map<String, String> dadosReserva) {
        String cpf = dadosReserva.get("cpf");
        String tipoRefeicao = dadosReserva.get("tipoRefeicao"); // Almoço ou Jantar

        // COMUNICAÇÃO ENTRE MICROSSERVIÇOS: A Reserva liga para o Pagamento via HTTP
        RestTemplate restTemplate = new RestTemplate();
        String urlPagamentoService = "http://localhost:8082/api/v1/pagamentos/sigaa/" + cpf;
        
        // Dispara o gatilho de rede e pega os dados do aluno do outro microsserviço
        Map<String, String> dadosAluno = restTemplate.getForObject(urlPagamentoService, Map.class);
        
        String categoria = dadosAluno.get("categoria");
        String valorRefeicao = dadosAluno.get("valorRefeicao");

        // Cria o recibo da reserva integrada
        Map<String, Object> respostaFinal = new HashMap<>();
        respostaFinal.put("reservaId", "RES-" + System.currentTimeMillis());
        respostaFinal.put("cpfAluno", cpf);
        respostaFinal.put("tipoRefeicao", tipoRefeicao);
        respostaFinal.put("categoriaIdentificada", categoria);
        respostaFinal.put("valorCobrado", valorRefeicao);
        
        if (categoria.equals("ISENTO")) {
            respostaFinal.put("statusReserva", "CONFIRMADA_AUTOMATICAMENTE");
            respostaFinal.put("mensagem", "Reserva liberada! Bom almoço.");
        } else {
            respostaFinal.put("statusReserva", "AGUARDANDO_PAGAMENTO");
            respostaFinal.put("mensagem", "Use o endpoint de pagamento para ler o QR Code do Pix.");
        }

        return respostaFinal;
    }
}