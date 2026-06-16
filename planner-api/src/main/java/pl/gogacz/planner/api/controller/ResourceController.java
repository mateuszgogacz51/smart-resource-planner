package pl.gogacz.planner.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.gogacz.planner.api.model.ResourceDTO;
import pl.gogacz.planner.api.model.ResourceRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
@Tag(name = "Zasoby", description = "Zarządzanie zasobami firmowymi (sprzęt, pojazdy, sale)")
public class ResourceController {

    @Operation(summary = "Pobierz wszystkie zasoby", description = "Zwraca listę wszystkich zasobów firmowych.")
    @GetMapping
    public ResponseEntity<List<ResourceDTO>> getAllResources() {
        // Tu zostanie wstrzyknięty serwis
        return ResponseEntity.ok(List.of());
    }

    @Operation(summary = "Pobierz zasób po ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Zasób znaleziony"),
        @ApiResponse(responseCode = "404", description = "Zasób nie istnieje")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResourceDTO> getResourceById(
            @Parameter(description = "ID zasobu", example = "1") @PathVariable Long id) {
        // Tu zostanie wstrzyknięty serwis
        return ResponseEntity.ok(new ResourceDTO());
    }

    @Operation(
        summary = "Dodaj nowy zasób",
        description = "Tworzy nowy zasób w systemie. Dostępne tylko dla roli ADMIN."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Zasób utworzony pomyślnie"),
        @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe"),
        @ApiResponse(responseCode = "401", description = "Brak autoryzacji")
    })
    @PostMapping
    public ResponseEntity<?> addResource(@Valid @RequestBody ResourceRequest request) {
        // Tu zostanie wstrzyknięty serwis
        return ResponseEntity.status(201).body(Map.of(
            "message", "Zasób '" + request.getName() + "' został dodany.",
            "category", request.getCategory()
        ));
    }

    @Operation(summary = "Aktualizuj zasób", description = "Aktualizuje dane istniejącego zasobu. Tylko ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Zasób zaktualizowany"),
        @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane"),
        @ApiResponse(responseCode = "404", description = "Zasób nie istnieje")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateResource(
            @Parameter(description = "ID zasobu do aktualizacji", example = "1") @PathVariable Long id,
            @Valid @RequestBody ResourceRequest request) {
        // Tu zostanie wstrzyknięty serwis
        return ResponseEntity.ok(Map.of("message", "Zasób #" + id + " zaktualizowany."));
    }

    @Operation(summary = "Usuń zasób", description = "Usuwa zasób z systemu. Tylko ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Zasób usunięty"),
        @ApiResponse(responseCode = "400", description = "Nie można usunąć — zasób ma aktywne rezerwacje"),
        @ApiResponse(responseCode = "404", description = "Zasób nie istnieje")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResource(
            @Parameter(description = "ID zasobu do usunięcia", example = "1") @PathVariable Long id) {
        // Tu zostanie wstrzyknięty serwis
        return ResponseEntity.ok(Map.of("message", "Zasób #" + id + " usunięty."));
    }
}
