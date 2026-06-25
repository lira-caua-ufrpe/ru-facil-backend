package br.ufrpe.rufacil.pagamento.model;

public class ResponseAlunoDTO {
    private String cpf;
    private String status;
    private String categoria;
    private String valorRefeicao;

    // Construtor padrão
    public ResponseAlunoDTO() {}

    // Construtor preenchido para facilitar o Mock
    public ResponseAlunoDTO(String cpf, String status, String categoria, String valorRefeicao) {
        this.cpf = cpf;
        this.status = status;
        this.categoria = categoria;
        this.valorRefeicao = valorRefeicao;
    }

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