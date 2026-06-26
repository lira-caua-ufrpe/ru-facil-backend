package br.ufrpe.rufacil.pagamento.service;

import br.ufrpe.rufacil.pagamento.model.AlunoDTO;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Simula a API do SIGAA/UFRPE para consulta de dados cadastrais do aluno.
 *
 * Categorias e valores do RU:
 *   ISENTO   → R$  0,00  (bolsistas, assistência estudantil)
 *   REGULAR  → R$  3,00  (alunos de graduação/pós em geral)
 *   EXTERNO  → R$ 20,00  (servidores, visitantes, comunidade externa)
 *
 * Em produção, este service faria uma chamada HTTP para a API REST do SIGAA
 * usando o token de integração institucional da UFRPE.
 */
@Service
public class SigaaMockService {

    // Base de alunos mock indexada por CPF
    private static final Map<String, AlunoDTO> BASE_ALUNOS = Map.of(
        "111",
        new AlunoDTO("111", "João Victor Silva",
                "joao.victor@ufrpe.br", "ATIVO", "ISENTO", "0.00"),

        "222",
        new AlunoDTO("222", "Maria Clara Souza",
                "maria.clara@ufrpe.br", "ATIVO", "REGULAR", "3.00"),

        "333",
        new AlunoDTO("333", "Carlos Eduardo Mendes",
                "carlos.mendes@gmail.com", "ATIVO", "EXTERNO", "20.00")
    );

    /**
     * Busca os dados do aluno pelo CPF.
     * @return Optional vazio se o CPF não existir no SIGAA.
     */
    public Optional<AlunoDTO> buscarPorCpf(String cpf) {
        return Optional.ofNullable(BASE_ALUNOS.get(cpf));
    }

    /**
     * Verifica se o aluno tem categoria isenta (não precisa de fichas).
     */
    public boolean isIsento(String cpf) {
        return buscarPorCpf(cpf)
                .map(a -> "ISENTO".equals(a.getCategoria()))
                .orElse(false);
    }
}
