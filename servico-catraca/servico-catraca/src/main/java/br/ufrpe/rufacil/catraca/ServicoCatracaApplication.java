package br.ufrpe.rufacil.catraca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "br.ufrpe.rufacil.catraca") // Garante que o Spring ache o pacote controller
public class ServicoCatracaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicoCatracaApplication.class, args);
    }

}