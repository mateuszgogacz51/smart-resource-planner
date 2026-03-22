CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    author VARCHAR(255),
    content TEXT,
    created_at TIMESTAMP,
    reservation_id BIGINT,
    CONSTRAINT fk_comments_reservation FOREIGN KEY (reservation_id) REFERENCES reservation(id) ON DELETE CASCADE
);