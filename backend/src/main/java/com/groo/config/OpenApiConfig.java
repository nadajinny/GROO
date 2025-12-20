package com.groo.config;

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

    private static final String SECURITY_SCHEME = "BearerAuth";

    @Bean
    public OpenAPI grooOpenAPI(JwtProperties jwtProperties) {
        return new OpenAPI()
                .info(new Info()
                        .title("GROO Collaboration Platform API")
                        .version("1.0.0")
                        .description("Lab collaboration backend API documentation.")
                        .contact(new Contact().name("GROO Backend").email("support@groo.local"))
                        .license(new License().name("MIT License")))
                .components(new Components().addSecuritySchemes(
                        SECURITY_SCHEME,
                        new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste JWT access token. Example: Bearer eyJhbGciOiJIUzI1NiJ9...")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME));
    }
}
