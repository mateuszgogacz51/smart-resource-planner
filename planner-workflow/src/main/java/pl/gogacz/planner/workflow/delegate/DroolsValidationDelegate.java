package pl.gogacz.planner.workflow.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;
import pl.gogacz.planner.api.model.ResourceDTO;

import java.math.BigDecimal;

@Component
public class DroolsValidationDelegate implements JavaDelegate {

    private final KieContainer kieContainer;

    public DroolsValidationDelegate(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long resId = (Long) execution.getVariable("resourceId");
        String resName = (String) execution.getVariable("resourceName");
        Double resValue = (Double) execution.getVariable("resourceValue");

        ResourceDTO resource = new ResourceDTO();
        resource.setId(resId);
        resource.setName(resName);
        if (resValue != null) {
            resource.setValue(BigDecimal.valueOf(resValue));
        }
        resource.setAvailable(true);

        KieSession kieSession = kieContainer.newKieSession();
        try {
            kieSession.insert(resource);
            kieSession.fireAllRules();
        } finally {
            kieSession.dispose();
        }

        boolean isApproved = resource.isAvailable();
        execution.setVariable("droolsApproval", isApproved);
        execution.setVariable("systemComment",
                "Drools validation finished. Final status: " + (isApproved ? "APPROVED" : "REJECTED"));
    }
}