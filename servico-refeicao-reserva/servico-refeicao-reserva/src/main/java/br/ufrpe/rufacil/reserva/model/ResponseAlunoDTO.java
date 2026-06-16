package br.ufrpe.rufacil.reserva.model;

public class ResponseAlunoDTO {
    private String cpf;
    private String status;
    private String categoria;
    private String valorRefeicao;

    // Getters e Setters
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getValorRefeicao() { return valorRefeicao; }
    public void setValorRefeicao(String valorRefeicao) { this.valorRefeicao = valorRefeicao; }
}