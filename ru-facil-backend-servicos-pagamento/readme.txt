=======================================================
  RU FÁCIL — Sistema de Gestão do Restaurante Universitário
  UFRPE — Universidade Federal Rural de Pernambuco
  Disciplina: Análise e Projeto de Sistemas OO
  Professor: Lucas Albertins
=======================================================

PRÉ-REQUISITOS
--------------
- Docker Desktop instalado e em execução
  Download: https://www.docker.com/products/docker-desktop
- Porta 8761, 8080, 8081, 8082, 8083, 8084 livres

COMO EXECUTAR
-------------
1. Abra o terminal (PowerShell no Windows ou Terminal no Mac/Linux)

2. Navegue até a pasta do projeto:
   cd ru-facil-backend-servicos-pagamento

3. Suba todos os serviços com um único comando:
   docker-compose up --build

   Aguarde até que todos os 6 serviços estejam em pé.
   O processo leva alguns minutos na primeira execução
   pois o Docker precisa compilar e baixar as dependências.

4. Verifique que todos os serviços estão registrados:
   Acesse: http://localhost:8761
   Você deve ver 5 instâncias registradas no painel do Eureka.

5. Todos os endpoints ficam disponíveis via API Gateway em:
   http://localhost:8080

PARA ENCERRAR
-------------
   docker-compose down

SERVIÇOS E PORTAS
-----------------
  Eureka (Service Discovery) : http://localhost:8761
  API Gateway                : http://localhost:8080
  Serviço de Reserva         : http://localhost:8081
  Serviço de Pagamento       : http://localhost:8082
  Serviço de Catraca         : http://localhost:8083
  Serviço de Acesso          : http://localhost:8084

ALUNOS DISPONÍVEIS NO MOCK SIGAA
---------------------------------
  CPF 111 — João Victor Silva    | ISENTO   | R$  0,00
  CPF 222 — Maria Clara Souza    | REGULAR  | R$  3,00
  CPF 333 — Carlos Eduardo Mendes| EXTERNO  | R$ 20,00

FLUXO DE TESTE — ALUNO ISENTO (CPF 111)
-----------------------------------------
  1. GET  http://localhost:8080/pagamentos/sigaa/111
  2. POST http://localhost:8080/catraca/passar
     Body: { "cpf": "111", "turno": "ALMOCO" }

FLUXO DE TESTE — ALUNO REGULAR (CPF 222)
------------------------------------------
  1. GET  http://localhost:8080/pagamentos/sigaa/222
  2. POST http://localhost:8080/pagamentos/processar
     Body: { "cpf": "222", "quantidade": 5 }
     → Retorna qrCodeBase64 e notaFiscalBase64
  3. POST http://localhost:8080/pagamentos/confirmar/{transacaoId}
  4. GET  http://localhost:8080/pagamentos/fichas/222
  5. POST http://localhost:8080/reservas
     Body: { "cpf": "222", "tipoRefeicao": "ALMOCO" }
  6. GET  http://localhost:8080/reservas/aluno/222
  7. POST http://localhost:8080/catraca/passar
     Body: { "cpf": "222", "turno": "ALMOCO" }
  8. DEL  http://localhost:8080/reservas/{reservaId}  (opcional)

CONSOLES H2 (banco em memória — acessar direto nos serviços)
-------------------------------------------------------------
  Pagamento : http://localhost:8082/h2-console
              JDBC URL: jdbc:h2:mem:pagamentodb
  Reserva   : http://localhost:8081/h2-console
              JDBC URL: jdbc:h2:mem:reservadb
  Usuário: sa | Senha: (vazio)

=======================================================
