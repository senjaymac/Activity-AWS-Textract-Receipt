package com.srllc.amazon_textract.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//Configuration Class for Swagger UI
@Configuration
public class OpenApiConfig {

    // Configuring custom open API documentation
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AWS Textract & Rekognition API with Spring Boot")
                        .version("1.0")
                        .description("""
                                API Documentation for AWS Textract receipt processing and Rekognition image analysis
                                
                                **Features:**
                                - Receipt text extraction and parsing with AWS Textract
                                - Image content analysis with AWS Rekognition
                                - Structured data storage in MySQL database
                                
                                **Developer:**
                                - Senon Jayson Macatangay
                                
                                """)
                        .contact(new Contact()
                                .name("Jayson Macatangay")
                                .email("senon.srbootcamp2025@gmail.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .name("Authorization")
                                .description("Bearer Token")));

    }
}

