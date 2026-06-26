package br.ufrpe.rufacil.pagamento.model;

/**
 * Representa os dados cadastrais do aluno vindos do SIGAA (mock).
 * Em produção este DTO seria preenchido pela API real do SIGAA/UFRPE.
 */
public class AlunoDTO {

    private String cpf;
    private String nome;
    private String email;
    private String status;       // ATIVO, INATIVO
    private String categoria;    // ISENTO, REGULAR, EXTERNO
    private String valorRefeicao; // "0.00", "3.00", "20.00"

    public AlunoDTO() {}

    public AlunoDTO(String cpf, String nome, String email,
                    String status, String categoria, String valorRefeicao) {
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.status = status;
        this.categoria = categoria;
        this.valorRefeicao = valorRefeicao;
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getValorRefeicao() { return valorRefeicao; }
    public void setValorRefeicao(String valorRefeicao) { this.valorRefeicao = valorRefeicao; }
}
