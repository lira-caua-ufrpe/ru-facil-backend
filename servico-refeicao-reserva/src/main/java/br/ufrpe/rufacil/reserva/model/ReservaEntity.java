package br.ufrpe.rufacil.reserva.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ReservaEntity {
    @Id
    private String id;
    private String cpfAluno;
    private String tipoRefeicao;
    private String categoriaIdentificada;
    private String valorCobrado;
    private String statusReserva;
    private String mensagem;

    public ReservaEntity() {}

    public ReservaEntity(String id, String cpfAluno, String tipoRefeicao, String categoriaIdentificada, String valorCobrado, String statusReserva, String mensagem) {
        this.id = id;
        this.cpfAluno = cpfAluno;
        this.tipoRefeicao = tipoRefeicao;
        this.categoriaIdentificada = categoriaIdentificada;
        this.valorCobrado = valorCobrado;
        this.statusReserva = statusReserva;
        this.mensagem = mensagem;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCpfAluno() { return cpfAluno; }
    public void setCpfAluno(String cpfAluno) { this.cpfAluno = cpfAluno; }
    public String getTipoRefeicao() { return tipoRefeicao; }
    public void setTipoRefeicao(String tipoRefeicao) { this.tipoRefeicao = tipoRefeicao; }
    public String getCategoriaIdentificada() { return categoriaIdentificada; }
    public void setCategoriaIdentificada(String categoriaIdentificada) { this.categoriaIdentificada = categoriaIdentificada; }
    public String getValorCobrado() { return valorCobrado; }
    public void setValorCobrado(String valorCobrado) { this.valorCobrado = valorCobrado; }
    public String getStatusReserva() { return statusReserva; }
    public void setStatusReserva(String statusReserva) { this.statusReserva = statusReserva; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}