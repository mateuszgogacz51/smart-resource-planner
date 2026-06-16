package pl.gogacz.planner.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.api.model.ReservationRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Rezerwacje", description = "Zarządzanie rezerwacjami zasobów firmowych")
public class ReservationController {

    @Operation(
        summary = "Utwórz nową rezerwację",
        description = "Składa wniosek o rezerwację zasobu. Wniosek trafia do procesu Camunda BPM, " +
                      "gdzie jest walidowany przez Drools i przekazywany do zatwierdzenia przez admina."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Wniosek złożony pomyślnie — uruchomiono proces zatwierdzania"),
        @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe (błędy walidacji)",
                     content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Brak autoryzacji — wymagane logowanie"),
        @ApiResponse(responseCode = "404", description = "Zasób o podanym ID nie istnieje")
    })
    @PostMapping
    public ResponseEntity<?> createReservation(@Valid @RequestBody ReservationRequest request) {
        // Tu zostanie wstrzyknięty serwis uruchamiający proces Camunda
        return ResponseEntity.ok(Map.of(
            "message", "Wniosek o rezerwację złożony pomyślnie. Oczekuje na zatwierdzenie.",
            "resourceId", request.getResourceId(),
            "requester", request.getRequesterUsername()
        ));
    }

    @Operation(
        summary = "Pobierz rezerwację po ID",
        description = "Zwraca szczegóły rezerwacji wraz z aktualnym statusem procesu Camunda."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rezerwacja znaleziona"),
        @ApiResponse(responseCode = "404", description = "Rezerwacja o podanym ID nie istnieje")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservation(
            @Parameter(description = "ID rezerwacji", example = "1") @PathVariable Long id) {
        // Tu zostanie wstrzyknięty serwis
        return ResponseEntity.ok(Map.of("id", id, "status", "PENDING"));
    }

    @Operation(
        summary = "Pobierz wszystkie rezerwacje użytkownika",
        description = "Zwraca listę rezerwacji złożonych przez danego użytkownika."
    )
    @GetMapping("/user/{username}")
    public ResponseEntity<?> getReservationsByUser(
            @Parameter(description = "Login użytkownika", example = "jan.kowalski")
            @PathVariable String username) {
        // Tu zostanie wstrzyknięty serwis
        return ResponseEntity.ok(Map.of("user", username, "reservations", java.util.List.of()));
    }

    @Operation(
        summary = "Anuluj rezerwację",
        description = "Anuluje oczekującą rezerwację. Możliwe tylko gdy status to PENDING."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rezerwacja anulowana"),
        @ApiResponse(responseCode = "400", description = "Nie można anulować — rezerwacja nie jest w statusie PENDING"),
        @ApiResponse(responseCode = "404", description = "Rezerwacja nie istnieje")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(
            @Parameter(description = "ID rezerwacji do anulowania", example = "1")
            @PathVariable Long id) {
        // Tu zostanie wstrzyknięty serwis
        return ResponseEntity.ok(Map.of("message", "Rezerwacja #" + id + " została anulowana."));
    }
}
