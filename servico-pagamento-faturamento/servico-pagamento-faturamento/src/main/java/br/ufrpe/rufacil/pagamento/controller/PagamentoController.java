package br.ufrpe.rufacil.pagamento.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ufrpe.rufacil.pagamento.model.ResponseAlunoDTO;

@RestController
@RequestMapping("/api/v1/pagamentos")
public class PagamentoController {

    // MOCK DO SIGAA (UC02 - Integração Síncrona que a Reserva chama)
    @GetMapping("/sigaa/{cpf}")
    public ResponseEntity<ResponseAlunoDTO> consultarSigaa(@PathVariable String cpf) {
        
        // Simulação de banco de dados/SIGAA com a Regra de Negócio do RU
        if ("111".equals(cpf)) {
            // Aluno Isento (Bolsista)
            return ResponseEntity.ok(new ResponseAlunoDTO(cpf, "ATIVO", "ISENTO", "0.00"));
        } else if ("222".equals(cpf)) {
            // Aluno Regular (Pagante)
            return ResponseEntity.ok(new ResponseAlunoDTO(cpf, "ATIVO", "REGULAR", "3.00"));
        } else {
            // CPF não encontrado no banco simulado do SIGAA
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // MOCK DO PAGBANK (UC03 - Processamento de pagamento via PIX)
    @PostMapping("/processar")
    public ResponseEntity<Map<String, Object>> processarPagamento(@RequestBody Map<String, String> dadosRequisicao) {
        String cpf = dadosRequisicao.get("cpf");
        
        Map<String, Object> respostaPix = new HashMap<>();
        respostaPix.put("transacaoId", "PAG-" + System.currentTimeMillis());
        respostaPix.put("cpfCliente", cpf);
        respostaPix.put("statusPagamento", "APROVADO_PAGBANK");
        respostaPix.put("qrCodePix", "00020101021126580014br.gov.bcb.pix0136rufacil-pagbank-mock-key");
        respostaPix.put("mensagem", "Pagamento processado com sucesso via Gateway!");

        return ResponseEntity.ok(respostaPix);
    }
}