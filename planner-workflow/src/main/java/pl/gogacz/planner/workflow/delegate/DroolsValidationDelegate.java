package pl.gogacz.planner.workflow.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;
import pl.gogacz.planner.api.model.ResourceDTO;

@Component
public class DroolsValidationDelegate implements JavaDelegate {

    private final KieContainer kieContainer;

    // --- RĘCZNY KONSTRUKTOR ZAMIAST LOMBOKA ---
    public DroolsValidationDelegate(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        ResourceDTO resource = (ResourceDTO) execution.getVariable("resource");

        KieSession kieSession = kieContainer.newKieSession();
        try {
            kieSession.insert(resource);
            kieSession.fireAllRules();
        } finally {
            kieSession.dispose();
        }

        boolean isApproved = resource.isAvailable(); // Teraz ta metoda na pewno istnieje!

        execution.setVariable("droolsApproval", isApproved);
        execution.setVariable("systemComment",
                "Drools validation finished. Final status: " + (isApproved ? "APPROVED" : "REJECTED"));
    }
}