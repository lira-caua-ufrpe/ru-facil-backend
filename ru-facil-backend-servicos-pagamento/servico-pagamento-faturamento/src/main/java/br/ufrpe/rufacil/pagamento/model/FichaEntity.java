package br.ufrpe.rufacil.pagamento.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDate;

/**
 * Representa o saldo de fichas de um aluno.
 * Cada ficha equivale a uma refeição pré-paga.
 *
 * Alunos ISENTOS nunca têm FichaEntity — eles passam direto na catraca.
 * Alunos REGULAR e EXTERNO compram fichas e o saldo é descontado a cada passagem.
 */
@Entity
public class FichaEntity {

    @Id
    private String cpf;         // CPF do aluno como chave primária

    private String nomeAluno;
    private String categoria;   // REGULAR ou EXTERNO
    private int fichasTotal;    // Total de fichas já compradas (histórico acumulado)
    private int fichasRestantes; // Saldo atual disponível
    private LocalDate validade; // Fim do semestre letivo atual

    public FichaEntity() {}

    public FichaEntity(String cpf, String nomeAluno, String categoria,
                       int fichasTotal, int fichasRestantes, LocalDate validade) {
        this.cpf = cpf;
        this.nomeAluno = nomeAluno;
        this.categoria = categoria;
        this.fichasTotal = fichasTotal;
        this.fichasRestantes = fichasRestantes;
        this.validade = validade;
    }

    // Getters e Setters
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getNomeAluno() { return nomeAluno; }
    public void setNomeAluno(String nomeAluno) { this.nomeAluno = nomeAluno; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public int getFichasTotal() { return fichasTotal; }
    public void setFichasTotal(int fichasTotal) { this.fichasTotal = fichasTotal; }
    public int getFichasRestantes() { return fichasRestantes; }
    public void setFichasRestantes(int fichasRestantes) { this.fichasRestantes = fichasRestantes; }
    public LocalDate getValidade() { return validade; }
    public void setValidade(LocalDate validade) { this.validade = validade; }
}
