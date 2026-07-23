ALTER TABLE notification_setting
    ADD COLUMN user_id BIGINT,
    ADD COLUMN pc_channel_mode VARCHAR(30) NOT NULL DEFAULT 'DESKTOP_FIRST',
    ADD COLUMN desktop_native_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN floating_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN urgent_auto_expand BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE user_notification_preference (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    apartment_id BIGINT NOT NULL,
    pc_channel_mode VARCHAR(30) NOT NULL DEFAULT 'DESKTOP_FIRST',
    desktop_native_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    floating_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    urgent_auto_expand BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_notification_preference_user_apartment UNIQUE (user_id, apartment_id)
);
