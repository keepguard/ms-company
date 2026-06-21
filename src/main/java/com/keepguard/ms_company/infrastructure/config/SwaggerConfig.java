package com.keepguard.ms_company.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KeepGuard Company API")
                        .version("1.0.0")
                        .description("API de gerenciamento de empresas e informações corporativas. " +
                                   "Esta API fornece funcionalidades de criação, atualização, consulta e " +
                                   "gerenciamento de empresas, incluindo dados de representantes, contatos, " +
                                   "endereços, contas bancárias e políticas corporativas.")
                        .contact(new Contact()
                                .name("KeepGuard Team")
                                .email("suporte@keepguard.com")
                                .url("https://keepguard.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8083")
                                .description("Servidor Local")
                ));
    }
}