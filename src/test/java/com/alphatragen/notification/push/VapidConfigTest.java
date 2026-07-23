package com.alphatragen.notification.push;

import com.alphatragen.notification.config.VapidConfig;
import nl.martijndwars.webpush.PushService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class VapidConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(VapidConfig.class);

    @Test
    void whenKeysArePresent_thenPushServiceIsCreated() {
        contextRunner.withPropertyValues(
                "app.vapid.public-key=BCJqbgnMJBUSN4VChYAQ1XmHeCy1-dL8EXhr1urZw5pP-RUnIluVV-q3sbw7yUyAfSt24r9pzFjgpW-bia0b8lA",
                "app.vapid.private-key=UaO5mP8qEqr3KJsUFGIiwhFV09lpQAKbSDIQJQDKLhM",
                "app.vapid.subject=mailto:test@test.com"
        ).run(context -> {
            assertThat(context).hasSingleBean(PushService.class);
        });
    }

    @Test
    void whenPublicKeyIsMissing_thenContextFails() {
        contextRunner.withPropertyValues(
                "app.vapid.public-key=",
                "app.vapid.private-key=UaO5mP8qEqr3KJsUFGIiwhFV09lpQAKbSDIQJQDKLhM",
                "app.vapid.subject=mailto:test@test.com"
        ).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining("VAPID configurations (public-key, private-key, subject) must not be empty");
        });
    }

    @Test
    void whenPrivateKeyIsMissing_thenContextFails() {
        contextRunner.withPropertyValues(
                "app.vapid.public-key=BCJqbgnMJBUSN4VChYAQ1XmHeCy1-dL8EXhr1urZw5pP-RUnIluVV-q3sbw7yUyAfSt24r9pzFjgpW-bia0b8lA",
                "app.vapid.private-key=",
                "app.vapid.subject=mailto:test@test.com"
        ).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining("VAPID configurations (public-key, private-key, subject) must not be empty");
        });
    }

    @Test
    void whenSubjectIsMissing_thenContextFails() {
        contextRunner.withPropertyValues(
                "app.vapid.public-key=BCJqbgnMJBUSN4VChYAQ1XmHeCy1-dL8EXhr1urZw5pP-RUnIluVV-q3sbw7yUyAfSt24r9pzFjgpW-bia0b8lA",
                "app.vapid.private-key=UaO5mP8qEqr3KJsUFGIiwhFV09lpQAKbSDIQJQDKLhM",
                "app.vapid.subject="
        ).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining("VAPID configurations (public-key, private-key, subject) must not be empty");
        });
    }
}
