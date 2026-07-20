-- V1__init_schema.sql

CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(100) NOT NULL UNIQUE,
    importance VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    source_type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    action_url VARCHAR(255),
    created_by BIGINT,
    retention_until TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification_target (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    apartment_id BIGINT NOT NULL,
    user_id BIGINT,
    building VARCHAR(50),
    unit VARCHAR(50),
    role VARCHAR(50),
    CONSTRAINT fk_notification_target_notification FOREIGN KEY (notification_id) REFERENCES notification(id) ON DELETE CASCADE
);

CREATE TABLE notification_recipient (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    recipient_user_id BIGINT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    push_sent_at TIMESTAMP,
    CONSTRAINT fk_notification_recipient_notification FOREIGN KEY (notification_id) REFERENCES notification(id) ON DELETE CASCADE,
    CONSTRAINT uq_notification_recipient UNIQUE (notification_id, recipient_user_id)
);

CREATE TABLE push_subscription (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    apartment_id BIGINT NOT NULL,
    endpoint VARCHAR(500) NOT NULL UNIQUE,
    p256dh VARCHAR(255) NOT NULL,
    auth VARCHAR(255) NOT NULL,
    browser VARCHAR(50),
    device_type VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification_setting (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL UNIQUE,
    retention_days INT NOT NULL DEFAULT 90,
    updated_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
