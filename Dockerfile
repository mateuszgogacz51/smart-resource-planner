# ETAP 1: Budowanie aplikacji (Maven)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# Kopiujemy wszystkie pliki projektu
COPY . .
# Budujemy cały projekt multi-module pomijając testy (żeby było szybciej)
RUN mvn clean package -DskipTests

# ETAP 2: Uruchomienie aplikacji (Lekkie środowisko Javy)
FROM eclipse-temurin:17-jre
WORKDIR /app
# Kopiujemy tylko wygenerowany plik .jar z poprzedniego etapu
COPY --from=build /app/planner-core/target/*.jar app.jar
EXPOSE 8080
# Odpalamy serwer
ENTRYPOINT ["java", "-jar", "app.jar"]