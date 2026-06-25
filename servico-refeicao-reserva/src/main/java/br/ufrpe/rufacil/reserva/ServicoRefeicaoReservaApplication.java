package br.ufrpe.rufacil.reserva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient // <--- ESSA ANOTAÇÃO AVISA O SPRING QUE ELE DEVE SE REGISTRAR NO EUREKA
public class ServicoRefeicaoReservaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicoRefeicaoReservaApplication.class, args);
    }

    // ADICIONADO AQUI: Configura o RestTemplate para traduzir "servico-pagamento" usando a lista do Eureka
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}