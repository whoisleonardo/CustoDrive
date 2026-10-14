CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    display_name VARCHAR(120) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Mantém os registros pré-existentes e permite atribuí-los ao primeiro usuário cadastrado.
ALTER TABLE cars ADD COLUMN owner_id BIGINT REFERENCES app_users(id) ON DELETE CASCADE;
CREATE INDEX idx_cars_owner_name ON cars (owner_id, name);
