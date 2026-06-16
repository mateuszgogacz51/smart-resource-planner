package pl.gogacz.planner.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartResourcePlannerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Smart Resource Planner API")
                .description(
                    "REST API systemu zarządzania zasobami firmowymi. " +
                    "Każda rezerwacja przechodzi przez proces zatwierdzania oparty na " +
                    "Camunda BPM i silniku reguł Drools."
                )
                .version("1.0.0")
                .contact(new Contact()
                    .name("Mateusz Gogacz")
                    .url("https://github.com/mateuszgogacz51/smart-resource-planner")
                )
            )
            // Obsługa JWT w Swaggerze — przycisk "Authorize" w UI
            .addSecurityItem(new SecurityRequirement().addList("JWT"))
            .components(new Components()
                .addSecuritySchemes("JWT", new SecurityScheme()
                    .name("JWT")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Wklej token JWT otrzymany po zalogowaniu. Bez prefiksu 'Bearer'.")
                )
            );
    }
}
