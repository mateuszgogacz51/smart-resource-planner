package pl.gogacz.planner.rules;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import pl.gogacz.planner.dto.ReservationRuleContext;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy integracyjne reguł Drools.
 * Ładują prawdziwy KieContainer z kmodule.xml i pliku .drl —
 * dzięki temu sprawdzamy faktyczną logikę reguł, nie mocki.
 */
class ReservationRulesTest {

    private KieContainer kieContainer;
    private KieSession kieSession;

    @BeforeEach
    void setUp() {
        KieServices ks = KieServices.Factory.get();
        kieContainer = ks.getKieClasspathContainer();
        kieSession = kieContainer.newKieSession("ksession-rules");
    }

    @AfterEach
    void tearDown() {
        if (kieSession != null) {
            kieSession.dispose();
        }
    }

    // -----------------------------------------------------------------------
    // REGUŁA 1: Brak podróży w czasie
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Reguła 1: Rezerwacja z datą startową w przeszłości powinna być odrzucona")
    void shouldRejectReservationWithStartDateInThePast() {
        // given
        ReservationRuleContext ctx = new ReservationRuleContext();
        ctx.setStartTime(LocalDateTime.now().minusDays(1));
        ctx.setEndTime(LocalDateTime.now().plusDays(5));
        ctx.setValid(true);

        // when
        kieSession.insert(ctx);
        kieSession.fireAllRules();

        // then
        assertThat(ctx.isValid()).isFalse();
        assertThat(ctx.getRejectionReason()).contains("datą wsteczną");
    }

    @Test
    @DisplayName("Reguła 1: Rezerwacja z datą startową w przyszłości powinna być zaakceptowana")
    void shouldAcceptReservationWithStartDateInTheFuture() {
        // given
        ReservationRuleContext ctx = new ReservationRuleContext();
        ctx.setStartTime(LocalDateTime.now().plusDays(1));
        ctx.setEndTime(LocalDateTime.now().plusDays(5));
        ctx.setValid(true);

        // when
        kieSession.insert(ctx);
        kieSession.fireAllRules();

        // then
        assertThat(ctx.isValid()).isTrue();
        assertThat(ctx.getRejectionReason()).isNull();
    }

    // -----------------------------------------------------------------------
    // REGUŁA 2: Odwrócone daty
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Reguła 2: Rezerwacja gdzie endTime jest przed startTime powinna być odrzucona")
    void shouldRejectReservationWithEndBeforeStart() {
        // given
        ReservationRuleContext ctx = new ReservationRuleContext();
        ctx.setStartTime(LocalDateTime.now().plusDays(5));
        ctx.setEndTime(LocalDateTime.now().plusDays(2)); // koniec przed startem
        ctx.setValid(true);

        // when
        kieSession.insert(ctx);
        kieSession.fireAllRules();

        // then
        assertThat(ctx.isValid()).isFalse();
        assertThat(ctx.getRejectionReason()).contains("Data zwrotu");
    }

    @Test
    @DisplayName("Reguła 2: Rezerwacja z poprawnymi datami (start < end) powinna przejść walidację")
    void shouldAcceptReservationWithCorrectDateOrder() {
        // given
        ReservationRuleContext ctx = new ReservationRuleContext();
        ctx.setStartTime(LocalDateTime.now().plusDays(1));
        ctx.setEndTime(LocalDateTime.now().plusDays(10));
        ctx.setValid(true);

        // when
        kieSession.insert(ctx);
        kieSession.fireAllRules();

        // then
        assertThat(ctx.isValid()).isTrue();
    }

    // -----------------------------------------------------------------------
    // REGUŁA 3: Maksymalny czas wypożyczenia 30 dni
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Reguła 3: Rezerwacja dłuższa niż 30 dni powinna być odrzucona")
    void shouldRejectReservationLongerThan30Days() {
        // given
        ReservationRuleContext ctx = new ReservationRuleContext();
        ctx.setStartTime(LocalDateTime.now().plusDays(1));
        ctx.setEndTime(LocalDateTime.now().plusDays(32)); // 31 dni
        ctx.setValid(true);

        // when
        kieSession.insert(ctx);
        kieSession.fireAllRules();

        // then
        assertThat(ctx.isValid()).isFalse();
        assertThat(ctx.getRejectionReason()).contains("30 dni");
    }

    @Test
    @DisplayName("Reguła 3: Rezerwacja dokładnie 30-dniowa powinna być zaakceptowana")
    void shouldAcceptReservationOfExactly30Days() {
        // given
        ReservationRuleContext ctx = new ReservationRuleContext();
        ctx.setStartTime(LocalDateTime.now().plusDays(1));
        ctx.setEndTime(LocalDateTime.now().plusDays(31)); // dokładnie 30 dni
        ctx.setValid(true);

        // when
        kieSession.insert(ctx);
        kieSession.fireAllRules();

        // then
        assertThat(ctx.isValid()).isTrue();
    }

    @Test
    @DisplayName("Reguła 3: Rezerwacja krótsza niż 30 dni powinna być zaakceptowana")
    void shouldAcceptReservationShorterThan30Days() {
        // given
        ReservationRuleContext ctx = new ReservationRuleContext();
        ctx.setStartTime(LocalDateTime.now().plusDays(1));
        ctx.setEndTime(LocalDateTime.now().plusDays(8)); // 7 dni
        ctx.setValid(true);

        // when
        kieSession.insert(ctx);
        kieSession.fireAllRules();

        // then
        assertThat(ctx.isValid()).isTrue();
    }
}
