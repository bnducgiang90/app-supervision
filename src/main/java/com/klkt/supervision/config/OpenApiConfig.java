package com.klkt.supervision.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "app-supervision API",
                version = "1.0.0",
                description = "API documentation for app-supervision Application",
                contact = @Contact(name = "ChatApp Team")
        ),
        security = {@SecurityRequirement(name = "bearerAuth")},
        servers = {@Server(url = "/", description = "Default Server")}
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Bearer token for authentication. Use 'Bearer <token>'",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
