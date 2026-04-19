package pl.gogacz.planner.core.service;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;
import pl.gogacz.planner.dto.ReservationRuleContext;

@Service
public class RuleService {

    private final KieContainer kieContainer;

    public RuleService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public void validateReservation(ReservationRuleContext context) {
        // Tu upewniamy się, że nazwa sesji to DOKŁADNIE ta sama, która jest w kmodule.xml
        KieSession kieSession = kieContainer.newKieSession("ksession-rules");
        try {
            kieSession.insert(context);
            kieSession.fireAllRules();
        } finally {
            kieSession.dispose();
        }
    }
}