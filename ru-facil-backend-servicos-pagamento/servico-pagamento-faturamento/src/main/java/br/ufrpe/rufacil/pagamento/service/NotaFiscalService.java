package br.ufrpe.rufacil.pagamento.service;

import br.ufrpe.rufacil.pagamento.model.AlunoDTO;
import br.ufrpe.rufacil.pagamento.model.PagamentoEntity;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

/**
 * Gera a Nota Fiscal em PDF usando iText 8 e simula o envio por email.
 *
 * O PDF é retornado como Base64 para o cliente poder baixar ou exibir.
 * O "envio de email" é simulado via log no console — em produção,
 * substituir pelo JavaMailSender com SMTP do Gmail.
 */
@Service
public class NotaFiscalService {

    private static final Logger log = LoggerFactory.getLogger(NotaFiscalService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Gera a nota fiscal em PDF e simula o envio por email.
     *
     * @param aluno      Dados do aluno vindos do SIGAA mock
     * @param pagamentos Lista de transações de pagamento do aluno
     * @param quantidadeFichasCompradas Fichas desta compra
     * @return PDF em Base64 (data:application/pdf;base64,...)
     */
    public String gerarEEnviarNotaFiscal(AlunoDTO aluno,
                                          List<PagamentoEntity> pagamentos,
                                          int quantidadeFichasCompradas) {
        try {
            byte[] pdfBytes = gerarPdf(aluno, pagamentos, quantidadeFichasCompradas);
            String pdfBase64 = "data:application/pdf;base64,"
                    + Base64.getEncoder().encodeToString(pdfBytes);

            // ── Simulação de envio de email ──────────────────────────────────
            log.info("═══════════════════════════════════════════════════════");
            log.info("  [EMAIL SIMULADO] Nota Fiscal gerada com sucesso!");
            log.info("  Para:    {}", aluno.getEmail());
            log.info("  Assunto: Nota Fiscal RU Fácil UFRPE - Compra de Fichas");
            log.info("  Aluno:   {} (CPF: {})", aluno.getNome(), aluno.getCpf());
            log.info("  Fichas:  {} ficha(s) comprada(s)", quantidadeFichasCompradas);
            log.info("  Total de transações na nota: {}", pagamentos.size());
            log.info("  Anexo:   nota-fiscal-{}.pdf", pagamentos.get(0).getTransacaoId());
            log.info("  [Em produção: envio real via JavaMailSender + SMTP Gmail]");
            log.info("═══════════════════════════════════════════════════════");

            return pdfBase64;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar nota fiscal: " + e.getMessage(), e);
        }
    }

    private byte[] gerarPdf(AlunoDTO aluno, List<PagamentoEntity> pagamentos,
                             int quantidadeFichas) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(40, 50, 40, 50);

        String numeroNota = "NF-" + System.currentTimeMillis();
        String dataEmissao = LocalDateTime.now().format(FMT);

        // ── Cabeçalho ────────────────────────────────────────────────────────
        Paragraph titulo = new Paragraph("RESTAURANTE UNIVERSITÁRIO — UFRPE")
                .setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER);
        Paragraph subtitulo = new Paragraph("RU Fácil — Sistema de Gestão de Refeições")
                .setFontSize(11).setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY);
        Paragraph notaFiscalTitulo = new Paragraph("NOTA FISCAL DE COMPRA DE FICHAS")
                .setBold().setFontSize(13).setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10);

        doc.add(titulo);
        doc.add(subtitulo);
        doc.add(notaFiscalTitulo);
        doc.add(new Paragraph("─".repeat(90)).setFontSize(8)
                .setFontColor(ColorConstants.GRAY));

        // ── Dados da nota ────────────────────────────────────────────────────
        doc.add(new Paragraph("Nº da Nota: " + numeroNota
                + "     Data de Emissão: " + dataEmissao)
                .setFontSize(10).setFontColor(ColorConstants.DARK_GRAY));

        doc.add(new Paragraph(" "));

        // ── Dados do aluno ───────────────────────────────────────────────────
        doc.add(new Paragraph("DADOS DO ESTUDANTE").setBold().setFontSize(11));

        Table tabelaAluno = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));

        adicionarLinha(tabelaAluno, "Nome completo:", aluno.getNome());
        adicionarLinha(tabelaAluno, "CPF:", formatarCpf(aluno.getCpf()));
        adicionarLinha(tabelaAluno, "E-mail:", aluno.getEmail());
        adicionarLinha(tabelaAluno, "Categoria:", aluno.getCategoria());
        adicionarLinha(tabelaAluno, "Valor por refeição:", "R$ " + aluno.getValorRefeicao());

        doc.add(tabelaAluno);
        doc.add(new Paragraph(" "));

        // ── Itens da nota ────────────────────────────────────────────────────
        doc.add(new Paragraph("ITENS ADQUIRIDOS").setBold().setFontSize(11));

        Table tabelaItens = new Table(
                UnitValue.createPercentArray(new float[]{10, 30, 20, 20, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        // Cabeçalho da tabela
        for (String col : new String[]{"#", "Descrição", "Qtd", "Valor Unit.", "Subtotal"}) {
            tabelaItens.addHeaderCell(
                new Cell().add(new Paragraph(col).setBold().setFontSize(10))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        double valorUnit = Double.parseDouble(aluno.getValorRefeicao());
        double totalGeral = 0;

        for (int i = 0; i < pagamentos.size(); i++) {
            PagamentoEntity p = pagamentos.get(i);
            double subtotal = valorUnit * quantidadeFichas;
            totalGeral += subtotal;

            tabelaItens.addCell(celula(String.valueOf(i + 1)));
            tabelaItens.addCell(celula("Fichas de refeição — " + aluno.getCategoria()));
            tabelaItens.addCell(celulaCenter(String.valueOf(quantidadeFichas)));
            tabelaItens.addCell(celulaCenter("R$ " + aluno.getValorRefeicao()));
            tabelaItens.addCell(celulaCenter(String.format("R$ %.2f", subtotal)));
        }

        doc.add(tabelaItens);
        doc.add(new Paragraph(" "));

        // ── Totais ───────────────────────────────────────────────────────────
        Table tabelaTotais = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(100));

        tabelaTotais.addCell(new Cell().add(
                new Paragraph("TOTAL GERAL").setBold().setFontSize(12))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        tabelaTotais.addCell(new Cell().add(
                new Paragraph(String.format("R$ %.2f", totalGeral)).setBold().setFontSize(12))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY));

        doc.add(tabelaTotais);
        doc.add(new Paragraph(" "));

        // ── Transações ───────────────────────────────────────────────────────
        doc.add(new Paragraph("HISTÓRICO DE TRANSAÇÕES").setBold().setFontSize(11));

        Table tabelaTx = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
                .setWidth(UnitValue.createPercentValue(100));

        for (String col : new String[]{"ID da Transação", "Status", "Método"}) {
            tabelaTx.addHeaderCell(
                new Cell().add(new Paragraph(col).setBold().setFontSize(9))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));
        }

        for (PagamentoEntity p : pagamentos) {
            tabelaTx.addCell(celula(p.getTransacaoId()));
            tabelaTx.addCell(celulaCenter(p.getStatusPagamento()));
            tabelaTx.addCell(celulaCenter("PIX"));
        }

        doc.add(tabelaTx);
        doc.add(new Paragraph(" "));

        // ── Rodapé ───────────────────────────────────────────────────────────
        doc.add(new Paragraph("─".repeat(90)).setFontSize(8)
                .setFontColor(ColorConstants.GRAY));
        doc.add(new Paragraph(
                "Este documento é uma simulação gerada pelo sistema RU Fácil para fins acadêmicos.\n"
                + "UFRPE — Universidade Federal Rural de Pernambuco | Recife - PE")
                .setFontSize(8).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return out.toByteArray();
    }

    // ── Helpers de célula ────────────────────────────────────────────────────

    private void adicionarLinha(Table tabela, String chave, String valor) {
        tabela.addCell(new Cell().add(new Paragraph(chave).setBold().setFontSize(10)));
        tabela.addCell(new Cell().add(new Paragraph(valor).setFontSize(10)));
    }

    private Cell celula(String texto) {
        return new Cell().add(new Paragraph(texto).setFontSize(9));
    }

    private Cell celulaCenter(String texto) {
        return new Cell().add(new Paragraph(texto).setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private String formatarCpf(String cpf) {
        // Para CPFs reais (11 dígitos); para os mocks curtos, retorna como está
        if (cpf != null && cpf.length() == 11) {
            return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                    + cpf.substring(6, 9) + "-" + cpf.substring(9);
        }
        return cpf;
    }
}
