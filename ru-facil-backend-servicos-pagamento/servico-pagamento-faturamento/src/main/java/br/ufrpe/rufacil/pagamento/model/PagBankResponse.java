package br.ufrpe.rufacil.pagamento.model;

/**
 * Representa a resposta simulada do PagBank após processar um pagamento Pix.
 * Em produção, este DTO mapearia o JSON real da API PagBank Orders v4.
 */
public class PagBankResponse {

    private String id;
    private String status;
    private String pixCopiaECola;
    private String qrCodeBase64;

    public PagBankResponse() {}

    public PagBankResponse(String id, String status, String pixCopiaECola, String qrCodeBase64) {
        this.id = id;
        this.status = status;
        this.pixCopiaECola = pixCopiaECola;
        this.qrCodeBase64 = qrCodeBase64;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPixCopiaECola() { return pixCopiaECola; }
    public void setPixCopiaECola(String pixCopiaECola) { this.pixCopiaECola = pixCopiaECola; }
    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }
}
