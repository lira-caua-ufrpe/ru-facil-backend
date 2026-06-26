package br.ufrpe.rufacil.pagamento.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PagamentoEntity {

    @Id
    private String transacaoId;

    private String cpfCliente;
    private String statusPagamento;

    /** Código Pix Copia e Cola (EMV/BR Code) gerado via mock PagBank */
    @Column(length = 1000)
    private String pixCopiaECola;

    /** QR Code em Base64 (PNG 300x300) gerado via ZXing — use como src de <img> */
    @Column(length = 10000)
    private String qrCodeBase64;

    private String mensagem;

    public PagamentoEntity() {}

    public PagamentoEntity(String transacaoId, String cpfCliente, String statusPagamento,
                           String pixCopiaECola, String qrCodeBase64, String mensagem) {
        this.transacaoId = transacaoId;
        this.cpfCliente = cpfCliente;
        this.statusPagamento = statusPagamento;
        this.pixCopiaECola = pixCopiaECola;
        this.qrCodeBase64 = qrCodeBase64;
        this.mensagem = mensagem;
    }

    // Getters e Setters
    public String getTransacaoId() { return transacaoId; }
    public void setTransacaoId(String transacaoId) { this.transacaoId = transacaoId; }
    public String getCpfCliente() { return cpfCliente; }
    public void setCpfCliente(String cpfCliente) { this.cpfCliente = cpfCliente; }
    public String getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(String statusPagamento) { this.statusPagamento = statusPagamento; }
    public String getPixCopiaECola() { return pixCopiaECola; }
    public void setPixCopiaECola(String pixCopiaECola) { this.pixCopiaECola = pixCopiaECola; }
    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
