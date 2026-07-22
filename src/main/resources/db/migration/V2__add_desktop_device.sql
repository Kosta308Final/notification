CREATE TABLE desktop_device (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    apartment_id BIGINT NOT NULL,
    device_id VARCHAR(100) NOT NULL UNIQUE,
    device_name VARCHAR(100) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    app_version VARCHAR(50) NOT NULL,
    notification_permission VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_connected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
