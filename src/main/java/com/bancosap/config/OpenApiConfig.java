package com.bancosap.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Banco SAP - API Bancária Demonstrativa")
                        .version("1.0.0")
                        .description("API RESTful corporativa para simulação bancária, PIX, gestão de contas, cartões virtuais, criptoativos demonstrativos e painel administrativo.")
                        .contact(new Contact()
                                .name("Equipe de Engenharia Banco SAP")
                                .email("contato@bancosap.com.br")
                                .url("https://bancosap.com.br"))
                        .license(new License()
                                .name("Uso Demonstrativo / Educacional")
                                .url("https://bancosap.com.br/termos")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o token JWT gerado no endpoint de login para autenticar as requisições.")));
    }
}
