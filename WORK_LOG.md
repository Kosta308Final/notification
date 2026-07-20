# Work Log

## [2026-07-19]
- Implemented Phase 14 logging and exception handling:
  - Added common JSON error responses with stable codes for validation, authentication, authorization, not found, conflict, and internal errors.
  - Added safe Spring Security 401/403 JSON responses and prevented internal exception details from reaching clients.
  - Added structured logs for notification creation, duplicate events, recipient counts, manual sends, Kafka final retries, and Push success/failure totals.
  - Removed Push endpoint logging and kept JWT, Push keys, and notification bodies out of logs.
  - Added `GlobalExceptionHandlerTest` and completed the Phase 14 checklist.

## [2026-07-19]
- Implemented Phase 13 PWA and PC web integration with a vanilla JavaScript notification center.
  - Added Service Worker push handling, active-window messages, system notification click navigation, same-origin URL validation, and duplicate notification suppression.
  - Added browser permission UX, VAPID subscription registration, existing subscription reuse, unread count refresh, notification filtering, pagination, accordion read handling, urgent styling, and logout endpoint deactivation.
  - Added an authenticated VAPID public-key endpoint and permitted static frontend resources.
  - Added executable-jar smoke verification for the page, service worker, manifest, and protected API, plus JavaScript syntax validation.
  - Phase 13 checklist completed.

## [2026-07-19]
- Implemented Phase 12 JWT authentication and authorization:
  - Added OAuth2 resource-server JWT security and unified 401/403 responses.
  - Extracted userId, apartmentId, and roles from JWT claims.
  - Replaced client-controlled identity and role headers in notification, push subscription, manual notification, and retention setting controllers.
  - Added repository-level user and apartment filtering for single-notification read operations to prevent IDOR and cross-apartment access.
  - Added JWT claim extraction tests and verified `classes` and `testClasses` compile successfully.
  - Phase 12 checklist completed.

## [2026-07-19]
- Implemented Phase 11 retention settings and cleanup batch:
  - Added retention setting query and update APIs with a 90-day default, 30-365 day validation, apartment isolation, administrator role checks, and updater tracking.
  - Updated domain and manual notification creation to calculate `retentionUntil` from the notification `createdAt` timestamp.
  - Added a daily scheduled cleanup service that deletes expired notifications and relies on cascading relationships for targets and recipients.
  - Added service tests for default settings, updates, authorization, validation, and cleanup result handling.
  - Phase 11 checklist completed.

## [2026-07-19]
- Implemented Phase 10 management-office manual notification APIs:
  - Added recipient preview at `POST /api/admin/notifications/recipients/preview` with target validation, deduplication, role authorization, and apartment scope checks.
  - Added immediate manual send at `POST /api/admin/notifications` with title/content, importance, target conditions, internal actionUrl validation, retention period, server-generated event ID, administrator tracking, recipient persistence, and post-commit Push event publishing.
  - Added `created_by` to the notification entity and initial schema migration.
  - Added `NotificationAdminServiceTest` covering preview, manual creation, recipient records, authorization, and apartment isolation.
  - Phase 10 checklist completed.
- Enabled JPA Auditing by introducing JpaConfig.java.
- Implemented and executed TDD Repository Unit Tests for all Phase 2 entities using Spring Boot integration tests (`@SpringBootTest` and `@Transactional`):
  - `NotificationRepositoryTest.java`: Tested notification persistence, unique event ID constraint, default values, target cascading saving, and cascade deletion.
  - `NotificationRecipientRepositoryTest.java`: Verified composite unique constraints, multi-user recipient storage, and read status/timestamp transitions.
  - `PushSubscriptionRepositoryTest.java`: Verified browser subscription persistence, unique endpoints, multiple devices per user, and activation status modification.
  - `NotificationSettingRepositoryTest.java`: Validated default retention settings, out-of-range checks (30-365 days), and apartment-specific configurations.
- Implemented Phase 3 (Notification Templates) following TDD practices:
  - Created `TemplateResult` data representation.
  - Created `NotificationTemplateService` interface and its implementation `NotificationTemplateServiceImpl` supporting formatting templates for:
    - `COMPLAINT_STATUS_CHANGED`
    - `COMPLAINT_ANSWER_REGISTERED`
    - `FACILITY_REQUEST_APPROVED`
    - `FACILITY_REQUEST_REJECTED`
    - `FACILITY_RESERVATION_CANCELLED_BY_ADMIN`
    - `VOTE_STARTED`
    - `VOTE_END_IMMINENT`
    - `VOTE_RESULT_PUBLISHED`
    - `URGENT_NOTICE`
    - `OFFICE_MANUAL_SEND` (manual override)
  - Enforced required template parameter checks and threw descriptive `IllegalArgumentException` on violations.
  - Wrote comprehensive TDD unit tests in `NotificationTemplateServiceTest.java`.
- All test suites successfully passed.
- Checked off Phase 2 and Phase 3 checklist items in `docs/TASKS.md`.
- Implemented Phase 4 (Target User Resolution):
  - Created `UserServiceClient` interface for integrating user queries.
  - Implemented `TargetResolver` and individual resolvers for all target types (`INDIVIDUAL`, `HOUSEHOLD`, `BUILDING`, `ROLE`, `APARTMENT`) preventing cross-apartment user leakage and enforcing parameters validation.
  - Created `NotificationTargetResolverComposite` to compose resolvers, filter out invalid/null user IDs, and perform user deduplication.
- Implemented Phase 5 (Notification Application Service):
  - Created `FakeUserServiceClient.java` as a default component so that the Spring boot application context boots up successfully.
  - Created `PushSender` interface and `ConsolePushSender` as a default Console-based implementation.
  - Created `NotificationCreatedEvent` and `NotificationCreatedEventListener` with `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` to process Web Push requests only after the DB transaction has successfully committed, ensuring push failures do not rollback the main transaction.
  - Created `NotificationApplicationService` to coordinate event validation, idempotency check, template generation, retention calculation (based on `NotificationSetting`), recipient resolution, persistence of Notification, Target, and Recipient entities in a single database transaction, and publishing the push notification event.
  - Wrote comprehensive TDD integration tests in `NotificationApplicationServiceTest.java` verifying creation success, idempotency, transaction rollback on database/resolver failure, push delivery after commit, and push exception isolation.
- Checked off Phase 5 items in `docs/TASKS.md` and verified all 52 tests successfully passed.
- Implemented Phase 6 (Kafka Consumption & Retry):
  - Configured project dependencies for Testcontainers (Kafka) and Spring Boot Testcontainers in `build.gradle`.
  - Created `KafkaConfig.java` to set up Kafka container factories with `DefaultErrorHandler` configuring a 2-retry policy (max 3 attempts total) with fixed backoff matching `app.kafka.retry-backoff-ms` environment variables. Included permanent failure logs for the recoverer.
  - Implemented `NotificationEventConsumer.java` listening on the `notification-events` topic, calling `NotificationApplicationService` to parse DTO, process notification creation/idempotency, log operations, and skip fatal `IllegalArgumentException` validation exceptions safely to prevent infinite retries.
  - Developed comprehensive TDD integration tests in `NotificationEventConsumerTest.java` running on a real Testcontainers Kafka instance. Tested successful consumption, invalid DTO skipping, duplicate event idempotency, and the 2-retry limits when processing exceptions occur.
  - All test suites compiled and executed successfully. Verified that Phase 6 tasks are complete.
- Implemented Phase 7 (User Notification Query and Read operations):
  - Created query methods in `NotificationRecipientRepository` supporting paginated lists (filtering by read/unread, sorted with urgent-unread first and then newest first), counting unread notifications, and bulk marking notifications as read.
  - Implemented `NotificationUserService` class containing all business logic for retrieval, unread count, single notification read processing (enforcing user access control and idempotency preserving original `readAt`), and bulk read processing.
  - Created `NotificationResponseDto` representing details of user notifications to be returned by REST endpoints.
  - Created `NotificationController` exposing endpoints at `/api/notifications` using header-based authentication parameters (`X-User-Id` and `X-Apartment-Id`) for Phase 7.
  - Wrote Mockito unit tests in `NotificationControllerTest` and Spring Boot integration tests in `NotificationUserServiceTest` validating correctness, sorting rules, filter, unread count, authorization check, and idempotency.
  - Disabled Kafka container auto-startup in application test profile by default, explicitly enabling it in `NotificationEventConsumerTest`, which drastically speeds up and stabilizes the local test lifecycle.
  - Verified all tests execute and compile successfully. Marked Phase 7 tasks as complete.
- Implemented Phase 9 (Web Push Sending):
  - Configured project dependencies for the Java Web Push library `nl.martijndwars:web-push`, `org.bouncycastle:bcprov-jdk18on`, `org.apache.httpcomponents:httpclient`, and `org.bitbucket.b_c:jose4j` in `build.gradle`.
  - Created `VapidConfig.java` to dynamically load VAPID settings (public/private key, subject), register BouncyCastle cryptographic provider, and expose the `PushService` bean.
  - Implemented `WebPushSender.java` implementing the `PushSender` interface to query active subscriptions, construct JSON payloads (including title, body, importance, notification ID, action URL), and send Web Push notifications using `PushService`.
  - Implemented expired subscription deactivation: handles Gone (410) and Not Found (404) status responses by setting `isActive = false` on the subscription, while keeping it active on transient network/connection exceptions.
  - Configured test profiles and added the consumer deserialization config in `application.yaml` to ensure Spring Boot's Testcontainers Kafka consumer tests compile and execute cleanly.
  - Wrote comprehensive TDD unit tests in `VapidConfigTest.java` and `WebPushSenderTest.java` validating correct initialization, missing config handling, active subscription matching, failure isolation, and expired endpoint deactivation.
  - All test suites execute and compile successfully. Marked Phase 9 tasks as complete.
2026-07-20 12 - Added Kafka consumer normalization for main-service notification JSON events, including nested recipients, offset timestamps, event aliases, and string-based deserialization.
