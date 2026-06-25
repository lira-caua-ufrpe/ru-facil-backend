package br.ufrpe.rufacil.pagamento.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PagamentoEntity {
    @Id
    private String transacaoId;
    private String cpfCliente;
    private String statusPagamento;
    private String qrCodePix;
    private String mensagem;

    public PagamentoEntity() {}

    public PagamentoEntity(String transacaoId, String cpfCliente, String statusPagamento, String qrCodePix, String mensagem) {
        this.transacaoId = transacaoId;
        this.cpfCliente = cpfCliente;
        this.statusPagamento = statusPagamento;
        this.qrCodePix = qrCodePix;
        this.mensagem = mensagem;
    }

    // Getters e Setters
    public String getTransacaoId() { return transacaoId; }
    public void setTransacaoId(String transacaoId) { this.transacaoId = transacaoId; }
    public String getCpfCliente() { return cpfCliente; }
    public void setCpfCliente(String cpfCliente) { this.cpfCliente = cpfCliente; }
    public String getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(String statusPagamento) { this.statusPagamento = statusPagamento; }
    public String getQrCodePix() { return qrCodePix; }
    public void setQrCodePix(String qrCodePix) { this.qrCodePix = qrCodePix; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}