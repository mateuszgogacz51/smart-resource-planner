package pl.gogacz.planner.workflow.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.gogacz.planner.api.model.ResourceDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DroolsValidationDelegateTest {

    @Mock
    private KieContainer kieContainer;

    @Mock
    private KieSession kieSession;

    @Mock
    private DelegateExecution execution;

    @InjectMocks
    private DroolsValidationDelegate delegate;

    @BeforeEach
    void setUp() {
        when(kieContainer.newKieSession()).thenReturn(kieSession);
    }

    @Test
    @DisplayName("execute: powinien ustawić droolsApproval=true gdy zasób jest dostępny")
    void shouldSetDroolsApprovalTrueWhenResourceAvailable() throws Exception {
        // given
        when(execution.getVariable("resourceId")).thenReturn(1L);
        when(execution.getVariable("resourceName")).thenReturn("Laptop Dell");
        when(execution.getVariable("resourceValue")).thenReturn(3500.0);

        // ResourceDTO.isAvailable() domyślnie zwraca true (ustawione w delegacie)
        // Drools nie zmienia nic (mocki nie uruchamiają reguł)

        // when
        delegate.execute(execution);

        // then
        verify(execution).setVariable("droolsApproval", true);
    }

    @Test
    @DisplayName("execute: powinien ustawić systemComment ze statusem APPROVED gdy zasób dostępny")
    void shouldSetSystemCommentApprovedWhenResourceAvailable() throws Exception {
        // given
        when(execution.getVariable("resourceId")).thenReturn(1L);
        when(execution.getVariable("resourceName")).thenReturn("Laptop Dell");
        when(execution.getVariable("resourceValue")).thenReturn(3500.0);

        // when
        delegate.execute(execution);

        // then
        ArgumentCaptor<String> commentCaptor = ArgumentCaptor.forClass(String.class);
        verify(execution).setVariable(eq("systemComment"), commentCaptor.capture());
        assertThat(commentCaptor.getValue()).contains("APPROVED");
    }

    @Test
    @DisplayName("execute: powinien obsłużyć null resourceValue bez NullPointerException")
    void shouldHandleNullResourceValueGracefully() throws Exception {
        // given
        when(execution.getVariable("resourceId")).thenReturn(2L);
        when(execution.getVariable("resourceName")).thenReturn("Sala konferencyjna A");
        when(execution.getVariable("resourceValue")).thenReturn(null); // brak wartości

        // when & then — nie powinno rzucić wyjątku
        delegate.execute(execution);

        verify(execution).setVariable(eq("droolsApproval"), anyBoolean());
    }

    @Test
    @DisplayName("execute: powinien zawsze wywołać dispose() na sesji Drools")
    void shouldAlwaysDisposeKieSession() throws Exception {
        // given
        when(execution.getVariable("resourceId")).thenReturn(1L);
        when(execution.getVariable("resourceName")).thenReturn("Samochód");
        when(execution.getVariable("resourceValue")).thenReturn(50000.0);

        // when
        delegate.execute(execution);

        // then
        verify(kieSession, times(1)).dispose();
    }

    @Test
    @DisplayName("execute: powinien wywołać fireAllRules() na sesji Drools")
    void shouldFireAllRules() throws Exception {
        // given
        when(execution.getVariable("resourceId")).thenReturn(1L);
        when(execution.getVariable("resourceName")).thenReturn("Samochód");
        when(execution.getVariable("resourceValue")).thenReturn(null);

        // when
        delegate.execute(execution);

        // then
        verify(kieSession).fireAllRules();
    }
}
