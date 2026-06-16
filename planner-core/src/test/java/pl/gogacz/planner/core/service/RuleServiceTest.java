package pl.gogacz.planner.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.gogacz.planner.dto.ReservationRuleContext;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleServiceTest {

    @Mock
    private KieContainer kieContainer;

    @Mock
    private KieSession kieSession;

    @InjectMocks
    private RuleService ruleService;

    @BeforeEach
    void setUp() {
        when(kieContainer.newKieSession("ksession-rules")).thenReturn(kieSession);
    }

    @Test
    @DisplayName("validateReservation: powinien otworzyć sesję Drools, wstrzyknąć kontekst i uruchomić reguły")
    void shouldOpenSessionInsertContextAndFireRules() {
        // given
        ReservationRuleContext context = new ReservationRuleContext();

        // when
        ruleService.validateReservation(context);

        // then
        verify(kieContainer).newKieSession("ksession-rules");
        verify(kieSession).insert(context);
        verify(kieSession).fireAllRules();
    }

    @Test
    @DisplayName("validateReservation: powinien zawsze zamknąć sesję Drools (dispose), nawet przy błędzie")
    void shouldAlwaysDisposeSession() {
        // given
        ReservationRuleContext context = new ReservationRuleContext();
        doThrow(new RuntimeException("Błąd reguły")).when(kieSession).fireAllRules();

        // when
        try {
            ruleService.validateReservation(context);
        } catch (RuntimeException ignored) {}

        // then — dispose musi zostać wywołane mimo wyjątku
        verify(kieSession).dispose();
    }

    @Test
    @DisplayName("validateReservation: powinien wywołać dispose dokładnie raz przy normalnym przebiegu")
    void shouldDisposeSessionOnce() {
        // given
        ReservationRuleContext context = new ReservationRuleContext();

        // when
        ruleService.validateReservation(context);

        // then
        verify(kieSession, times(1)).dispose();
    }
}
