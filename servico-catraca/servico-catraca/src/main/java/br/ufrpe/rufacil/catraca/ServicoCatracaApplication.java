package br.ufrpe.rufacil.catraca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = "br.ufrpe.rufacil.catraca")
@EnableDiscoveryClient
public class ServicoCatracaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicoCatracaApplication.class, args);
    }

    // ADICIONE ESTE BEAN AQUI: Ele faz a tradução automática do nome lógico para a porta real
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}