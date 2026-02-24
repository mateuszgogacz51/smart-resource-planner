package pl.gogacz.planner.core.config;

import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaConfig {

    @Bean
    public ProcessEnginePlugin spinProcessEnginePlugin() {
        return new SpinProcessEnginePlugin();
    }
}