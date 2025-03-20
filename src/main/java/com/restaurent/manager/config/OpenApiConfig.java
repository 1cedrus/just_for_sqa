package com.restaurent.manager.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Nguyen Dinh Hoan",
                        email = "dinhhoan0511@gmail.com"
                ),
                description = "OpenAPI for SEP490-G11",
                title = "OpenAPI SEP490-G11",
                version = "1.0",
                license = @License(
                        name = "License name"
                ),
                termsOfService = "termOfService"
        ),
        servers = {
                @Server(
                        description = "Local Server",
                        url = "http://vietkitchen.shop/"
                ),
                @Server(
                        description = "Local Be Server",
                        url = "http://be.vietkitchen.shop/"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
