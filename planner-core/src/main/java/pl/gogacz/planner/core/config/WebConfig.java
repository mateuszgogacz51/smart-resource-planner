package pl.gogacz.planner.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Zezwól na wszystkie endpointy (np. /api/**)
                .allowedOrigins("http://localhost:4200") // Adres, pod którym będzie żył Angular
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // Kluczowe! Pozwala Angularowi wysyłać Twoje hasło w tle
    }
}