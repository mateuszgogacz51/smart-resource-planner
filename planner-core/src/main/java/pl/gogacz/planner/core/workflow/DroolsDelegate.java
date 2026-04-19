package pl.gogacz.planner.core.workflow;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

// Ta adnotacja to magiczne powiązanie z Twoim ${droolsDelegate} w Modelerze!
@Component("droolsDelegate")
public class DroolsDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        System.out.println("=========================================");
        System.out.println("🚀 CAMUNDA URUCHOMIŁA KROK: Weryfikacja Drools!");

        // Pobieramy ID rezerwacji (będziemy je przekazywać z Frontendu)
        Long reservationId = (Long) execution.getVariable("reservationId");
        System.out.println("🔍 Analizuję rezerwację nr: " + reservationId);

        // TUTAJ W KOLEJNYM KROKU ODPALIMY SILNIK DROOLS
        // Na ten moment symulujemy, że reguły biznesowe przepuściły wniosek:
        boolean rulesPassed = true;

        // Zapisujemy wynik z powrotem do Camundy
        execution.setVariable("rulesPassed", rulesPassed);

        System.out.println("✅ Weryfikacja zakończona. Wynik: " + rulesPassed);
        System.out.println("=========================================");
    }
}