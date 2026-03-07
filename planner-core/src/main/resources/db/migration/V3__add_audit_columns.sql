-- Dodanie kolumn dla automatycznych dat utworzenia i edycji
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE reservation ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;