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

    public ReservationRuleContext validateReservation(ReservationRuleContext context) {
        KieSession kieSession = kieContainer.newKieSession();
        try {
            kieSession.insert(context);
            kieSession.fireAllRules();
        } finally {
            kieSession.dispose();
        }
        return context;
    }
}