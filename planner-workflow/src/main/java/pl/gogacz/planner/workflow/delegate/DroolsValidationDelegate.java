package pl.gogacz.planner.workflow.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;
import pl.gogacz.planner.api.model.ResourceDTO;

import lombok.RequiredArgsConstructor;

@Component("droolsValidationDelegate")
@RequiredArgsConstructor
public class DroolsValidationDelegate implements JavaDelegate {

    private final KieContainer kieContainer;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // 1. Pobranie danych o zasobie z procesu Camunda
        ResourceDTO resource = (ResourceDTO) execution.getVariable("resource");

        // 2. Uruchomienie sesji Drools
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert(resource);
        kieSession.fireAllRules();
        kieSession.dispose();

        // 3. Aktualizacja zmiennych procesu na podstawie wyniku reguł
        execution.setVariable("resource", resource);
        execution.setVariable("isAvailable", resource.isAvailable());
        execution.setVariable("resourceValue", resource.getValue());

        System.out.println("LOG: Drools zakończył walidację dla: " + resource.getName());
    }
}