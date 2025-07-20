CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE devices (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    mac_address VARCHAR(17) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT,
    CONSTRAINT fk_device_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE plants (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    species VARCHAR(255) NOT NULL,
    watering_frequency INT NOT NULL,
    ideal_soil_moisture DOUBLE PRECISION NOT NULL,
    current_soil_moisture DOUBLE PRECISION,
    current_temperature DOUBLE PRECISION,
    image_url VARCHAR(512),
    last_watered TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    CONSTRAINT fk_plant_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_plant_device FOREIGN KEY (device_id) REFERENCES devices(id)
);

CREATE TABLE irrigation_history (
    id SERIAL PRIMARY KEY,
    water_volume DOUBLE PRECISION NOT NULL,
    soil_moisture DOUBLE PRECISION NOT NULL,
    duration_seconds INT,
    temperature DOUBLE PRECISION,
    is_automatic BOOLEAN,
    humidity_percent DOUBLE PRECISION,
    irrigation_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    plant_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    CONSTRAINT fk_irrigation_plant FOREIGN KEY (plant_id) REFERENCES plants(id),
    CONSTRAINT fk_irrigation_device FOREIGN KEY (device_id) REFERENCES devices(id)
);

CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255),
    message TEXT NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    notification_type VARCHAR(50) NOT NULL,
    priority VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE password_reset_tokens (
    id SERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);