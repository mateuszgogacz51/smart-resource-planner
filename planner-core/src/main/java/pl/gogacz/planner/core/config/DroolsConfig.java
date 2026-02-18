package pl.gogacz.planner.core.config;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfig {

    @Bean
    public KieContainer kieContainer() {
        // Ta metoda automatycznie skanuje classpath w poszukiwaniu plików .drl
        // znajdujących się w folderze resources/rules innych modułów (np. planner-rules)
        return KieServices.Factory.get().getKieClasspathContainer();
    }
}