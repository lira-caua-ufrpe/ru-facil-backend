package br.ufrpe.rufacil.pagamento;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagamentos")
public class PagamentoController {

    // MOCK DO SIGA UFRPE (UC01 - Validar Categoria do Aluno)
    @GetMapping("/sigaa/{cpf}")
    public Map<String, String> consultarSigaa(@PathVariable String cpf) {
        Map<String, String> resposta = new HashMap<>();
        resposta.put("cpf", cpf);
        
        // Simulação de regras de negócio baseadas no CPF enviado
        if (cpf.equals("111")) {
            resposta.put("status", "ATIVO");
            resposta.put("categoria", "ISENTO");
            resposta.put("valorRefeicao", "0.00");
        } else if (cpf.equals("222")) {
            resposta.put("status", "ATIVO");
            resposta.put("categoria", "PAGANTE_REDUZIDO");
            resposta.put("valorRefeicao", "3.00");
        } else {
            resposta.put("status", "ATIVO");
            resposta.put("categoria", "PAGANTE_INTEIRA");
            resposta.put("valorRefeicao", "10.00");
        }
        return resposta;
    }

    // MOCK DO PAGBANK + GOOGLE CHARTS (UC04 - Processar Pagamento e Gerar QR Code)
    @PostMapping("/processar")
    public Map<String, Object> processarPagamento(@RequestBody Map<String, String> dadosRequisicao) {
        String cpf = dadosRequisicao.get("cpf");
        String valor = dadosRequisicao.get("valor");
        
        Map<String, Object> respostaTransacao = new HashMap<>();
        respostaTransacao.put("transacaoId", "PAG-" + System.currentTimeMillis());
        respostaTransacao.put("status", "CONCLUIDO"); // Simula aprovação imediata do PagBank
        respostaTransacao.put("valorPago", valor);
        
        // Integração conceitual com a API do Google Charts para gerar a imagem do QR Code
        String urlQrCode = "https://chart.googleapis.com/chart?chs=300x300&cht=qr&chl=RUFACIL-PIX-VALOR-" + valor + "-CPF-" + cpf;
        respostaTransacao.put("qrCodeUrl", urlQrCode);
        
        return respostaTransacao;
    }
}