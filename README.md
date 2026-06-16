# 📅 Smart Resource Planner

> Platforma full-stack do zarządzania zasobami firmowymi (samochody, laptopy, sale konferencyjne) z automatycznym workflow zatwierdzania rezerwacji — zbudowana w oparciu o Spring Boot 3.2, Camunda BPM 7.20 i Angular.

---

## 📋 Spis treści

- [Opis projektu](#opis-projektu)
- [Funkcjonalności](#funkcjonalności)
- [Architektura techniczna](#architektura-techniczna)
- [Wymagania środowiskowe](#wymagania-środowiskowe)
- [Instrukcja uruchomienia](#instrukcja-uruchomienia)
- [Dokumentacja API](#dokumentacja-api)
- [Testy](#testy)
- [Role i uprawnienia](#role-i-uprawnienia)
- [Stos technologiczny](#stos-technologiczny)

---

## 📌 Opis projektu

**Smart Resource Planner** to enterprise'owa platforma do zarządzania zasobami firmy. Pracownicy mogą rezerwować zasoby (np. samochód służbowy, laptopa, salę konferencyjną), a każda rezerwacja przechodzi przez zautomatyzowany proces zatwierdzania z wykorzystaniem silnika procesów biznesowych **Camunda BPM**. Reguły biznesowe (np. priorytety, limity, konflikty rezerwacji) obsługiwane są przez silnik reguł **Drools**. System zapewnia pełną transparentność — każda rezerwacja ma swój stan w procesie BPMN, historię zdarzeń i powiadomienia e-mail.

---

## ✨ Funkcjonalności

### 📦 Zarządzanie zasobami

- **Katalog zasobów** — przeglądanie dostępnych zasobów firmowych (pojazdy, sprzęt IT, sale)
- **Typy zasobów** — podział na kategorie z osobnymi regułami dostępności
- **Status zasobu** — śledzenie czy zasób jest wolny, zarezerwowany lub niedostępny
- **Zarządzanie pulą zasobów** — admin dodaje, edytuje i usuwa zasoby z systemu

### 📋 System rezerwacji

- **Tworzenie rezerwacji** — pracownik składa wniosek o zasób podając zakres dat i cel
- **Konflikt rezerwacji** — system automatycznie wykrywa nakładające się rezerwacje
- **Historia rezerwacji** — pełny rejestr wszystkich wniosków użytkownika
- **Anulowanie rezerwacji** — możliwe gdy status to PENDING

### ⚙️ Workflow zatwierdzania (Camunda BPM)

- **Automatyczny proces BPMN** — każda rezerwacja uruchamia instancję procesu w Camundzie
- **Etapy procesu:** Złożenie wniosku → Weryfikacja Drools → Oczekiwanie na zatwierdzenie → Zatwierdzona / Odrzucona
- **Task list dla adminów** — panel zadań do ręcznego zatwierdzania wniosków
- **Historia procesów** — pełny audit trail każdej instancji procesu

### 🧠 Silnik reguł biznesowych (Drools)

- **Automatyczna weryfikacja wniosków** — reguły sprawdzają wniosek przed przekazaniem do admina
- **Reguła 1 — brak podróży w czasie** — odrzucenie rezerwacji z datą wsteczną
- **Reguła 2 — odwrócone daty** — odrzucenie gdy data zwrotu jest przed datą wypożyczenia
- **Reguła 3 — limit 30 dni** — odrzucenie rezerwacji dłuższej niż 30 dni
- **Konfigurowalne reguły** — admin może modyfikować reguły bez zmiany kodu (pliki `.drl`)

### 🔐 Bezpieczeństwo i walidacja

- **JWT Authentication** — bezstanowe tokeny autoryzacyjne w każdym żądaniu API
- **Spring Security** — ochrona endpointów na poziomie backendu
- **Bean Validation** — walidacja DTO (`@NotNull`, `@NotBlank`, `@Future`, `@DecimalMin`) przed przetworzeniem
- **GlobalExceptionHandler** — spójne odpowiedzi błędów JSON zamiast stacktrace'ów Springa

### 📧 Powiadomienia

- **E-mail przy zmianie statusu** — pracownik otrzymuje powiadomienie o zatwierdzeniu/odrzuceniu
- **Reset hasła przez admina** — automatyczny e-mail z tymczasowym hasłem

---

## 🏗️ Architektura techniczna

Projekt oparty na **wielomodułowej architekturze Maven** z wyraźnym rozdzieleniem warstw.

```
┌──────────────────────────────────────────────────────────┐
│                    smart-resource-planner                │
│  (Maven Multi-Module Parent)                             │
├──────────────┬───────────────┬──────────────┬───────────┤
│ planner-api  │ planner-core  │planner-rules │planner-   │
│ (REST API /  │ (Encje, Repo, │ (Drools -    │workflow   │
│  Kontrolery, │  Serwisy,     │  reguły .drl)│(Camunda   │
│  Swagger)    │  Security)    │              │ BPMN)     │
└──────┬───────┴───────────────┴──────────────┴───────────┘
       │
┌──────▼───────┐     ┌──────────────┐     ┌──────────────┐
│  planner-ui  │     │  PostgreSQL  │     │  Camunda DB  │
│  (Angular)   │     │  (dane app)  │     │  (procesy)   │
└──────────────┘     └──────────────┘     └──────────────┘
```

### Moduły projektu

| Moduł | Odpowiedzialność |
|-------|-----------------|
| `planner-parent` | Konfiguracja nadrzędna Maven, zarządzanie wersjami zależności |
| `planner-core` | Encje JPA, repozytoria, serwisy biznesowe, Spring Security + JWT |
| `planner-api` | Kontrolery REST API, DTOs z walidacją, Swagger/OpenAPI, obsługa błędów |
| `planner-rules` | Silnik reguł Drools — pliki `.drl` z logiką biznesową |
| `planner-workflow` | Integracja Camunda BPM — pliki `.bpmn`, delegaty procesów |
| `planner-ui` | Frontend Angular — komponenty, serwisy HTTP, routing |

---

## ⚙️ Wymagania środowiskowe

| Wymaganie | Wersja minimalna |
|-----------|-----------------|
| JDK | 17+ |
| Node.js | 18+ |
| PostgreSQL | 13+ |
| Maven | 3.8+ |

---

## 🚀 Instrukcja uruchomienia

### Opcja 1 — Docker Compose (zalecana)

```bash
docker-compose up --build
```

Aplikacja dostępna pod adresem: `http://localhost:4200`

Panel Camunda (cockpit/tasklist): `http://localhost:8080/camunda`

### Opcja 2 — Ręczne uruchomienie

**1. Baza danych**
Skonfiguruj PostgreSQL w `planner-core/src/main/resources/application.properties`.

**2. Backend**
```bash
./mvnw spring-boot:run -pl planner-core
```

**3. Frontend**
```bash
cd planner-ui
npm install
ng serve
```

Domyślne dane administratora:
```
Login: admin
Hasło: admin123
```

---

## 📖 Dokumentacja API

Swagger UI dostępny po uruchomieniu backendu pod adresem:

```
http://localhost:8080/swagger-ui/index.html
```

Dokumentacja zawiera wszystkie endpointy z opisami, przykładami requestów i możliwością testowania bezpośrednio w przeglądarce. Aby testować chronione endpointy — kliknij przycisk **Authorize** i wklej token JWT otrzymany po zalogowaniu.

### Dostępne endpointy

| Metoda | Endpoint | Opis |
|--------|----------|------|
| `GET` | `/api/resources` | Lista wszystkich zasobów |
| `GET` | `/api/resources/{id}` | Szczegóły zasobu |
| `POST` | `/api/resources` | Dodaj zasób (ADMIN) |
| `PUT` | `/api/resources/{id}` | Aktualizuj zasób (ADMIN) |
| `DELETE` | `/api/resources/{id}` | Usuń zasób (ADMIN) |
| `POST` | `/api/reservations` | Złóż wniosek o rezerwację |
| `GET` | `/api/reservations/{id}` | Szczegóły rezerwacji |
| `GET` | `/api/reservations/user/{username}` | Rezerwacje użytkownika |
| `DELETE` | `/api/reservations/{id}` | Anuluj rezerwację |

---

## 🧪 Testy

Projekt zawiera testy jednostkowe i integracyjne pokrywające kluczowe warstwy aplikacji.

### Uruchomienie testów

```bash
# Wszystkie testy
./mvnw test

# Tylko wybrany moduł
./mvnw test -pl planner-core
./mvnw test -pl planner-rules
./mvnw test -pl planner-workflow
```

### Pokrycie testami

| Plik testu | Typ | Co testuje |
|------------|-----|------------|
| `RuleServiceTest` | Jednostkowy (Mockito) | Otwieranie sesji Drools, `fireAllRules()`, `dispose()` przy błędzie |
| `EmailServiceTest` | Jednostkowy (Mockito) | Adresat maila, treść z hasłem, temat, liczba wywołań `send()` |
| `ReservationRulesTest` | Integracyjny (prawdziwy KieContainer) | Wszystkie 3 reguły `.drl` — przypadki pozytywne i negatywne |
| `DroolsValidationDelegateTest` | Jednostkowy (Mockito) | Zmienne procesowe Camundy, `droolsApproval`, null handling |

### Stos testowy

- **JUnit 5** — framework testowy
- **Mockito** — mockowanie zależności
- **AssertJ** — czytelne asercje

---

## 👤 Role i uprawnienia

| Funkcja | USER | ADMIN |
|---------|:----:|:-----:|
| Przeglądanie zasobów | ✅ | ✅ |
| Składanie wniosków o rezerwację | ✅ | ✅ |
| Podgląd własnych rezerwacji | ✅ | ✅ |
| Anulowanie własnej rezerwacji | ✅ | ✅ |
| Zatwierdzanie/odrzucanie rezerwacji | ❌ | ✅ |
| Zarządzanie katalogiem zasobów | ❌ | ✅ |
| Zarządzanie użytkownikami | ❌ | ✅ |
| Podgląd procesów Camunda (cockpit) | ❌ | ✅ |
| Reset hasła użytkownika | ❌ | ✅ |

---

## 🛠️ Stos technologiczny

| Warstwa | Technologia |
|---------|-------------|
| Backend | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Silnik procesów | Camunda BPM 7.20 (BPMN 2.0) |
| Silnik reguł | Drools 8.44 (reguły `.drl`) |
| Baza danych | PostgreSQL, Flyway (migracje), H2 (testy) |
| Autoryzacja | JWT (jjwt 0.11.5) |
| Dokumentacja API | SpringDoc OpenAPI 2.3.0 (Swagger UI) |
| Walidacja | Bean Validation (`jakarta.validation`) |
| Frontend | Angular, TypeScript, RxJS |
| Build | Maven (multi-moduł) |
| Konteneryzacja | Docker, Docker Compose |
| Testy | JUnit 5, Mockito, AssertJ |
| Narzędzia | Lombok |

---

## 📁 Struktura projektu

```
smart-resource-planner/
├── planner-core/                   # Główny moduł — serwisy, encje, security
│   └── src/
│       ├── main/java/.../
│       │   ├── model/              # Encje JPA
│       │   ├── repository/         # Interfejsy Spring Data JPA
│       │   ├── service/            # Logika biznesowa (RuleService, EmailService)
│       │   └── security/           # JWT, Spring Security config
│       └── test/java/.../
│           └── service/            # RuleServiceTest, EmailServiceTest
├── planner-api/                    # REST API
│   └── src/main/java/.../
│       ├── controller/             # ReservationController, ResourceController
│       ├── model/                  # DTOs z walidacją (ReservationRequest, ResourceRequest)
│       ├── config/                 # OpenApiConfig (Swagger)
│       └── exception/              # GlobalExceptionHandler, ResourceNotFoundException
├── planner-rules/                  # Silnik reguł Drools
│   └── src/
│       ├── main/resources/rules/   # Pliki .drl z regułami biznesowymi
│       └── test/java/.../          # ReservationRulesTest (testy integracyjne)
├── planner-workflow/               # Procesy Camunda BPM
│   └── src/
│       ├── main/
│       │   ├── java/.../delegate/  # DroolsValidationDelegate
│       │   └── resources/          # Pliki .bpmn
│       └── test/java/.../          # DroolsValidationDelegateTest
├── planner-ui/                     # Frontend Angular
│   └── src/app/
├── Dockerfile
├── docker-compose.yml
└── pom.xml                         # Parent POM
```

---

*Smart Resource Planner — inteligentne zarządzanie zasobami firmowymi z automatycznym workflow Camunda BPM i silnikiem reguł Drools.*
