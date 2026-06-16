package br.ufrpe.rufacil.pagamento.controller;

import br.ufrpe.rufacil.pagamento.model.ResponseAlunoDTO;
import br.ufrpe.rufacil.pagamento.model.PagamentoEntity;
import br.ufrpe.rufacil.pagamento.repository.PagamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pagamentos")
public class PagamentoController {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @GetMapping("/sigaa/{cpf}")
    public ResponseEntity<ResponseAlunoDTO> consultarSigaa(@PathVariable String cpf) {
        if ("111".equals(cpf)) {
            return ResponseEntity.ok(new ResponseAlunoDTO(cpf, "ATIVO", "ISENTO", "0.00"));
        } else if ("222".equals(cpf)) {
            return ResponseEntity.ok(new ResponseAlunoDTO(cpf, "ATIVO", "REGULAR", "3.00"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/processar")
    public ResponseEntity<PagamentoEntity> processarPagamento(@RequestBody Map<String, String> dadosRequisicao) {
        String cpf = dadosRequisicao.get("cpf");
        String idTx = "PAG-" + System.currentTimeMillis();
        String qrCode = "00020101021126580014br.gov.bcb.pix0136rufacil-pagbank-mock-key";
        String msg = "Pagamento processado com sucesso via Gateway!";

        // PERSISTÊNCIA REAL AQUI: Gravando a transação Pix no banco de dados H2 do Pagamento
        PagamentoEntity novoPagamento = new PagamentoEntity(idTx, cpf, "APROVADO_PAGBANK", qrCode, msg);
        pagamentoRepository.save(novoPagamento);

        return ResponseEntity.ok(novoPagamento);
    }

    // NOVO ENDPOINT PARA LISTAR TRANSAÇÕES DO BANCO: Controle de dados garantido
    @GetMapping("/transacoes")
    public ResponseEntity<List<PagamentoEntity>> listarTodasAsTransacoes() {
        return ResponseEntity.ok(pagamentoRepository.findAll());
    }
}