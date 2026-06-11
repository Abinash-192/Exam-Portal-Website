package com.examportal.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;

@Configuration
public class OpenApiConfig {

    private  static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI examPortalOpenAPI(){
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("paste your JWT token here"))
                );
    }

    private Info apiInfo(){
        return new Info()
                .title("ExamPortal REST API")
                .description("Smart examination portal - Admin & Student endpoints")
                .version("1.0.0")
                .contact(new Contact()
                        .name("ExamPortal Team")
                        .email("support@examportal.com")
                        .url("https://examportal.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

}
