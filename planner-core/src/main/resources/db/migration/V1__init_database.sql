-- Tworzenie tabeli Użytkowników
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255)
);

-- Tworzenie tabeli Ról dla użytkowników
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    roles VARCHAR(255),
    CONSTRAINT fk_user_roles FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Tworzenie tabeli Rezerwacji z audytem
CREATE TABLE reservation (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT,
    user_id VARCHAR(255),
    start_time TIMESTAMP(6),
    end_time TIMESTAMP(6),
    status VARCHAR(255),
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6)
);