package train.local.fogpass.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "EOEL-FogPASS Master Data API",
                version = "1.0.0",
                description = "Comprehensive API documentation for EOEL-FogPASS Master Data Management System",
                contact = @Contact(
                        name = "FogPASS Development Team",
                        email = "support@fogpass.com"
                ),
                license = @License(
                        name = "Proprietary",
                        url = "https://fogpass.com/license"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Development Server"),
                @Server(url = "https://api.fogpass.com", description = "Production Server")
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "JWT Authentication token. Login via /api/auth/login to obtain token."
)
public class OpenApiConfig {
}