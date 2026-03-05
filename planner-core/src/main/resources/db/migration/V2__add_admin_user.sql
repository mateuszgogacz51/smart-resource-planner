-- Czyścimy starą tabelę jeśli coś tam było (opcjonalnie)
DELETE FROM user_roles;
DELETE FROM users;

-- Dodajemy admina (hasło: admin123)
INSERT INTO users (id, username, password, first_name, last_name)
VALUES (1, 'admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMG.8D8nHP46', 'Admin', 'System');

-- Nadajemy rolę
INSERT INTO user_roles (user_id, roles)
VALUES (1, 'ROLE_ADMIN');