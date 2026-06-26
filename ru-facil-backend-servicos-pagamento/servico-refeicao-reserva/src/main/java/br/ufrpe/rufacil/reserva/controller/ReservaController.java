package br.ufrpe.rufacil.reserva.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.ufrpe.rufacil.reserva.client.FichaClient;
import br.ufrpe.rufacil.reserva.model.RequestReservaDTO;
import br.ufrpe.rufacil.reserva.model.ReservaEntity;
import br.ufrpe.rufacil.reserva.repository.ReservaRepository;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired private ReservaRepository reservaRepository;
    @Autowired private FichaClient fichaClient;

    // ── 1. Criar reserva ─────────────────────────────────────────────────────

    /**
     * Cria uma reserva de refeição.
     *
     * Fluxo por categoria:
     *   ISENTO  → reserva criada com status CONFIRMADA automaticamente (sem fichas)
     *   REGULAR/EXTERNO → verifica se tem saldo de fichas aprovadas no serviço de pagamento
     *                     Se tiver → CONFIRMADA; se não → AGUARDANDO_PAGAMENTO
     *
     * Body: { "cpf": "222", "tipoRefeicao": "ALMOCO" }
     */
    @PostMapping
    public ResponseEntity<?> criarReserva(@RequestBody RequestReservaDTO dados) {
        if (dados.getCpf() == null || dados.getTipoRefeicao() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "CPF e tipoRefeicao são obrigatórios."));
        }

        // Consulta dados do aluno no SIGAA (via servico-pagamento)
        Map<String, Object> aluno;
        try {
            aluno = fichaClient.consultarAluno(dados.getCpf());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("erro", "Serviço de pagamento indisponível: " + e.getMessage()));
        }

        String categoria = (String) aluno.getOrDefault("categoria", "REGULAR");
        String nomeAluno = (String) aluno.getOrDefault("nome", "Aluno");
        String valorRefeicao = (String) aluno.getOrDefault("valorRefeicao", "3.00");

        String idReserva = "RES" + System.currentTimeMillis();
        String status;
        String mensagem;

        if ("ISENTO".equals(categoria)) {
            // Isento: confirma direto, sem verificar fichas
            status = "CONFIRMADA_AUTOMATICAMENTE";
            mensagem = "Reserva confirmada! Acesso liberado na catraca. Bom almoço, " + nomeAluno + "!";
        } else {
            // Regular/Externo: verifica saldo de fichas
            int fichasRestantes = 0;
            try {
                Map<String, Object> fichas = fichaClient.consultarFichas(dados.getCpf());
                Object saldo = fichas.get("fichasRestantes");
                if (saldo != null) fichasRestantes = Integer.parseInt(saldo.toString());
            } catch (Exception ignored) {}

            if (fichasRestantes > 0) {
                status = "CONFIRMADA";
                mensagem = String.format(
                        "Reserva confirmada! Você tem %d ficha(s) disponível(is).", fichasRestantes);
            } else {
                status = "AGUARDANDO_PAGAMENTO";
                mensagem = "Sem fichas disponíveis. Compre fichas em /pagamentos/processar.";
            }
        }

        ReservaEntity novaReserva = new ReservaEntity(
                idReserva, dados.getCpf(), dados.getTipoRefeicao(),
                categoria, valorRefeicao, status, mensagem);
        reservaRepository.save(novaReserva);

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("reservaId", idReserva);
        resposta.put("cpf", dados.getCpf());
        resposta.put("nomeAluno", nomeAluno);
        resposta.put("tipoRefeicao", dados.getTipoRefeicao());
        resposta.put("categoria", categoria);
        resposta.put("statusReserva", status);
        resposta.put("mensagem", mensagem);
        resposta.put("valorRefeicao", "R$ " + valorRefeicao);
        return ResponseEntity.ok(resposta);
    }

    // ── 2. Cancelar reserva ──────────────────────────────────────────────────

    /**
     * Cancela uma reserva pelo ID.
     * Reservas já usadas (UTILIZADA) não podem ser canceladas.
     */
    @DeleteMapping("/{reservaId}")
    public ResponseEntity<?> cancelarReserva(@PathVariable String reservaId) {
        ReservaEntity reserva = reservaRepository.findById(reservaId).orElse(null);
        if (reserva == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Reserva não encontrada: " + reservaId));
        }
        if ("UTILIZADA".equals(reserva.getStatusReserva())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Reserva já utilizada não pode ser cancelada."));
        }
        if ("CANCELADA".equals(reserva.getStatusReserva())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("aviso", "Reserva já estava cancelada."));
        }

        reserva.setStatusReserva("CANCELADA");
        reservaRepository.save(reserva);

        return ResponseEntity.ok(Map.of(
                "reservaId", reservaId,
                "status",    "CANCELADA",
                "mensagem",  "Reserva cancelada com sucesso."
        ));
    }

    // ── 3. Validar para catraca ──────────────────────────────────────────────

    /**
     * Valida se o aluno pode passar na catraca para o turno informado.
     *
     * Fluxo:
     *   ISENTO  → passa direto (consulta categoria no SIGAA, sem reserva obrigatória)
     *   REGULAR/EXTERNO → precisa de reserva CONFIRMADA para o turno
     *                     Ao validar, marca a reserva como UTILIZADA
     */
    @GetMapping("/validar/{cpf}/{turno}")
    public ResponseEntity<?> validarParaCatraca(
            @PathVariable String cpf, @PathVariable String turno) {

        // Isento: libera sem verificar reserva
        try {
            Map<String, Object> aluno = fichaClient.consultarAluno(cpf);
            String categoria = (String) aluno.getOrDefault("categoria", "");
            if ("ISENTO".equals(categoria)) {
                return ResponseEntity.ok(Map.of(
                        "status",   "LIBERADO",
                        "categoria","ISENTO",
                        "mensagem", "Acesso liberado — aluno isento."));
            }
        } catch (Exception ignored) {
            // Se SIGAA indisponível, continua para verificar reserva normalmente
        }

        // Regular/Externo: busca reserva confirmada para o turno
        List<ReservaEntity> confirmadas = reservaRepository
                .findByCpfAlunoAndStatusReserva(cpf, "CONFIRMADA");
        confirmadas.addAll(reservaRepository
                .findByCpfAlunoAndStatusReserva(cpf, "CONFIRMADA_AUTOMATICAMENTE"));

        for (ReservaEntity res : confirmadas) {
            if (turno.equalsIgnoreCase(res.getTipoRefeicao())) {
                // Marca como utilizada para não permitir reentrada
                res.setStatusReserva("UTILIZADA");
                reservaRepository.save(res);

                return ResponseEntity.ok(Map.of(
                        "status",    "LIBERADO",
                        "reservaId", res.getId(),
                        "turno",     turno,
                        "mensagem",  "Acesso liberado. Bom apetite!"));
            }
        }

        return ResponseEntity.ok(Map.of(
                "status",   "BLOQUEADO",
                "turno",    turno,
                "mensagem", "Nenhuma reserva confirmada para o turno " + turno + "."));
    }

    // ── 4. Reservas do aluno ─────────────────────────────────────────────────

    /**
     * Retorna reservas ativas do aluno + saldo de fichas.
     * Reservas ativas = status CONFIRMADA ou AGUARDANDO_PAGAMENTO.
     */
    @GetMapping("/aluno/{cpf}")
    public ResponseEntity<?> reservasDoAluno(@PathVariable String cpf) {
        List<ReservaEntity> confirmadas = reservaRepository
                .findByCpfAlunoAndStatusReserva(cpf, "CONFIRMADA");
        confirmadas.addAll(reservaRepository
                .findByCpfAlunoAndStatusReserva(cpf, "CONFIRMADA_AUTOMATICAMENTE"));
        confirmadas.addAll(reservaRepository
                .findByCpfAlunoAndStatusReserva(cpf, "AGUARDANDO_PAGAMENTO"));

        int fichasRestantes = 0;
        String nomeAluno = "Aluno";
        String categoria = "";
        try {
            Map<String, Object> fichas = fichaClient.consultarFichas(cpf);
            Object saldo = fichas.get("fichasRestantes");
            if (saldo != null) fichasRestantes = Integer.parseInt(saldo.toString());

            Map<String, Object> aluno = fichaClient.consultarAluno(cpf);
            nomeAluno = (String) aluno.getOrDefault("nome", "Aluno");
            categoria  = (String) aluno.getOrDefault("categoria", "");
        } catch (Exception ignored) {}

        return ResponseEntity.ok(Map.of(
                "cpf",              cpf,
                "nomeAluno",        nomeAluno,
                "categoria",        categoria,
                "fichasRestantes",  fichasRestantes,
                "reservasAtivas",   confirmadas.size(),
                "reservas",         confirmadas
        ));
    }

    // ── 5. Listar todas ──────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<ReservaEntity>> listarTodas() {
        return ResponseEntity.ok(reservaRepository.findAll());
    }
}
