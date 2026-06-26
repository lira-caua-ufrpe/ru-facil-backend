package br.ufrpe.rufacil.pagamento.controller;

import java.time.LocalDate;
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

import br.ufrpe.rufacil.pagamento.model.AlunoDTO;
import br.ufrpe.rufacil.pagamento.model.FichaEntity;
import br.ufrpe.rufacil.pagamento.model.PagBankResponse;
import br.ufrpe.rufacil.pagamento.model.PagamentoEntity;
import br.ufrpe.rufacil.pagamento.repository.FichaRepository;
import br.ufrpe.rufacil.pagamento.repository.PagamentoRepository;
import br.ufrpe.rufacil.pagamento.service.NotaFiscalService;
import br.ufrpe.rufacil.pagamento.service.PagBankService;
import br.ufrpe.rufacil.pagamento.service.SigaaMockService;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    @Autowired private PagamentoRepository pagamentoRepository;
    @Autowired private FichaRepository fichaRepository;
    @Autowired private PagBankService pagBankService;
    @Autowired private SigaaMockService sigaaMockService;
    @Autowired private NotaFiscalService notaFiscalService;

    // ── 1. Consulta SIGAA ────────────────────────────────────────────────────

    @GetMapping("/sigaa/{cpf}")
    public ResponseEntity<AlunoDTO> consultarSigaa(@PathVariable String cpf) {
        return sigaaMockService.buscarPorCpf(cpf)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ── 2. Comprar fichas ────────────────────────────────────────────────────

    /**
     * Processa compra de fichas via Pix (mock PagBank).
     * Gera QR Code ZXing + nota fiscal PDF simulada.
     * Body: { "cpf": "222", "quantidade": 5 }
     */
    @PostMapping("/processar")
    public ResponseEntity<?> processarPagamento(@RequestBody Map<String, Object> body) {
        String cpf = (String) body.get("cpf");
        int quantidade = body.containsKey("quantidade")
                ? Integer.parseInt(body.get("quantidade").toString()) : 1;

        if (cpf == null || cpf.isBlank())
            return ResponseEntity.badRequest().body(Map.of("erro", "CPF é obrigatório."));
        if (quantidade < 1 || quantidade > 50)
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Quantidade deve ser entre 1 e 50 fichas."));

        AlunoDTO aluno = sigaaMockService.buscarPorCpf(cpf).orElse(null);
        if (aluno == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Aluno não encontrado no SIGAA. CPF: " + cpf));
        if ("ISENTO".equals(aluno.getCategoria()))
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Aluno ISENTO não precisa comprar fichas."));

        double valorUnit = Double.parseDouble(aluno.getValorRefeicao());
        String valorTotal = String.format("%.2f", valorUnit * quantidade).replace(",", ".");
        String transacaoId = "PAG" + System.currentTimeMillis();

        PagBankResponse pagBankResp = pagBankService.processarPagamento(cpf, valorTotal, transacaoId);

        PagamentoEntity pagamento = new PagamentoEntity(
                transacaoId, cpf,
                pagBankResp.getStatus(),
                pagBankResp.getPixCopiaECola(),
                pagBankResp.getQrCodeBase64(),
                String.format("Compra de %d ficha(s) — R$ %s", quantidade, valorTotal));
        pagamentoRepository.save(pagamento);

        String notaFiscalBase64 = notaFiscalService.gerarEEnviarNotaFiscal(
                aluno, List.of(pagamento), quantidade);

        return ResponseEntity.ok(Map.of(
                "transacaoId",      transacaoId,
                "cpf",              cpf,
                "nomeAluno",        aluno.getNome(),
                "categoria",        aluno.getCategoria(),
                "quantidadeFichas", quantidade,
                "valorTotal",       "R$ " + valorTotal,
                "status",           pagBankResp.getStatus(),
                "mensagem",         "Escaneie o QR Code para pagar. Depois confirme em /pagamentos/confirmar/" + transacaoId,
                "qrCodeBase64",     pagBankResp.getQrCodeBase64(),
                "notaFiscalBase64", notaFiscalBase64
        ));
    }

    // ── 3. Confirmar pagamento Pix (webhook simulado) ────────────────────────

    @PostMapping("/confirmar/{transacaoId}")
    public ResponseEntity<?> confirmarPagamento(@PathVariable String transacaoId) {
        PagamentoEntity pagamento = pagamentoRepository.findById(transacaoId).orElse(null);
        if (pagamento == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Transação não encontrada: " + transacaoId));
        if ("APROVADO".equals(pagamento.getStatusPagamento()))
            return ResponseEntity.badRequest()
                    .body(Map.of("aviso", "Pagamento já confirmado anteriormente."));

        pagamento.setStatusPagamento("APROVADO");
        pagamentoRepository.save(pagamento);

        int qtdFichas = extrairQuantidadeFichas(pagamento.getMensagem());

        AlunoDTO aluno = sigaaMockService.buscarPorCpf(pagamento.getCpfCliente()).orElse(null);
        String nomeAluno = aluno != null ? aluno.getNome() : "Desconhecido";
        String categoria = aluno != null ? aluno.getCategoria() : "REGULAR";

        LocalDate hoje = LocalDate.now();
        LocalDate validade = hoje.getMonthValue() <= 6
                ? LocalDate.of(hoje.getYear(), 7, 31)
                : LocalDate.of(hoje.getYear(), 12, 31);

        FichaEntity ficha = fichaRepository.findById(pagamento.getCpfCliente())
                .orElse(new FichaEntity(pagamento.getCpfCliente(), nomeAluno,
                        categoria, 0, 0, validade));
        ficha.setFichasTotal(ficha.getFichasTotal() + qtdFichas);
        ficha.setFichasRestantes(ficha.getFichasRestantes() + qtdFichas);
        fichaRepository.save(ficha);

        return ResponseEntity.ok(Map.of(
                "transacaoId",     transacaoId,
                "status",          "APROVADO",
                "fichasCreditas",  qtdFichas,
                "fichasRestantes", ficha.getFichasRestantes(),
                "validade",        validade.toString(),
                "mensagem",        "Pagamento confirmado! Fichas creditadas. Já pode reservar refeições."
        ));
    }

    // ── 4. Descontar ficha (chamado pela catraca) ────────────────────────────

    /**
     * Desconta 1 ficha do saldo do aluno.
     * Chamado automaticamente pelo servico-catraca após liberar o acesso.
     */
    @PostMapping("/fichas/descontar/{cpf}")
    public ResponseEntity<?> descontarFicha(@PathVariable String cpf) {
        FichaEntity ficha = fichaRepository.findById(cpf).orElse(null);
        if (ficha == null || ficha.getFichasRestantes() <= 0) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(Map.of("erro", "Sem fichas disponíveis para o CPF: " + cpf));
        }
        ficha.setFichasRestantes(ficha.getFichasRestantes() - 1);
        fichaRepository.save(ficha);

        return ResponseEntity.ok(Map.of(
                "cpf",             cpf,
                "fichasRestantes", ficha.getFichasRestantes(),
                "mensagem",        "Ficha descontada com sucesso."
        ));
    }

    // ── 5. QR Code de uma transação ──────────────────────────────────────────

    @GetMapping("/qrcode/{transacaoId}")
    public ResponseEntity<?> obterQrCode(@PathVariable String transacaoId) {
        return pagamentoRepository.findById(transacaoId)
                .map(p -> ResponseEntity.ok(Map.of(
                        "transacaoId",   p.getTransacaoId(),
                        "qrCodeBase64",  p.getQrCodeBase64(),
                        "pixCopiaECola", p.getPixCopiaECola(),
                        "status",        p.getStatusPagamento()
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ── 6. Saldo de fichas do aluno ──────────────────────────────────────────

    @GetMapping("/fichas/{cpf}")
    public ResponseEntity<?> consultarFichas(@PathVariable String cpf) {
        if (sigaaMockService.isIsento(cpf)) {
            return ResponseEntity.ok(Map.of(
                    "cpf", cpf,
                    "mensagem", "Aluno ISENTO — não utiliza fichas.",
                    "fichasRestantes", 0));
        }
        return fichaRepository.findById(cpf)
                .map(f -> ResponseEntity.ok(Map.of(
                        "cpf",             f.getCpf(),
                        "nomeAluno",       f.getNomeAluno(),
                        "categoria",       f.getCategoria(),
                        "fichasTotal",     f.getFichasTotal(),
                        "fichasRestantes", f.getFichasRestantes(),
                        "validade",        f.getValidade().toString()
                )))
                .orElse(ResponseEntity.ok(Map.of(
                        "cpf", cpf,
                        "fichasRestantes", 0,
                        "mensagem", "Nenhuma ficha comprada ainda."
                )));
    }

    // ── 7. Todas as transações (debug) ───────────────────────────────────────

    @GetMapping("/transacoes")
    public ResponseEntity<List<PagamentoEntity>> listarTransacoes() {
        return ResponseEntity.ok(pagamentoRepository.findAll());
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private int extrairQuantidadeFichas(String mensagem) {
        if (mensagem == null) return 1;
        try {
            String[] partes = mensagem.split("de ");
            if (partes.length > 1)
                return Integer.parseInt(partes[1].trim().split(" ")[0]);
        } catch (Exception ignored) {}
        return 1;
    }
}
