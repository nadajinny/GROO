package com.groo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
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

    @Bean
    public OpenApiCustomizer defaultResponsesCustomizer() {
        return openAPI -> {
            if (openAPI.getPaths() == null) {
                return;
            }
            for (PathItem pathItem : openAPI.getPaths().values()) {
                for (Operation operation : pathItem.readOperations()) {
                    ApiResponses responses = operation.getResponses();
                    ensureErrorResponse(responses, "400", "Request parameters are invalid.", "BAD_REQUEST");
                    ensureErrorResponse(responses, "401", "Authentication token is missing or expired.", "UNAUTHORIZED");
                    ensureErrorResponse(responses, "403", "You do not have permission for this action.", "FORBIDDEN");
                    ensureErrorResponse(responses, "404", "Requested resource was not found.", "NOT_FOUND");
                    ensureErrorResponse(responses, "422", "Input was well-formed but failed validation.", "UNPROCESSABLE_ENTITY");
                    ensureErrorResponse(responses, "500", "An unexpected server error occurred.", "INTERNAL_SERVER_ERROR");
                }
            }
        };
    }

    private void ensureErrorResponse(ApiResponses responses, String statusCode, String message, String code) {
        responses.computeIfAbsent(statusCode, key -> defaultErrorResponse(message, code));
    }

    private ApiResponse defaultErrorResponse(String message, String code) {
        Schema<?> schema = new ObjectSchema()
                .addProperty("success", new BooleanSchema().example(false))
                .addProperty("data", new ObjectSchema().nullable(true))
                .addProperty("message", new StringSchema().example(message))
                .addProperty("code", new StringSchema().example(code));

        MediaType mediaType = new MediaType()
                .schema(schema)
                .example(String.format(
                        "{\"success\":false,\"data\":null,\"message\":\"%s\",\"code\":\"%s\"}",
                        message,
                        code));

        Content content = new Content().addMediaType("application/json", mediaType);

        return new ApiResponse()
                .description(message)
                .content(content);
    }
}
