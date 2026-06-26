package br.ufrpe.rufacil.pagamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient // <--- ATIVA O CLIENTE DO EUREKA AQUI TAMBÉM
public class ServicoPagamentoFaturamentoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicoPagamentoFaturamentoApplication.class, args);
    }
}