# Project context (YNC Live Class)

Real-time class Q&A and polls. The instructor shows a live board on the projector (PC, login); students join from phones with a 6-digit code and a nickname (no login).

## Stack
- Spring Boot 3.5, Java 21 toolchain compiled for 17, Gradle
- spring-boot-starter-websocket, STOMP, built-in simple broker (plain WebSocket, **no SockJS**)
- Browser: `@stomp/stompjs` 7 (UMD, `new StompJs.Client({...})`), `wss://` when the page is https
- PostgreSQL 16 (Docker), JPA with `ddl-auto: validate`, schema in `docs/schema.sql`
- Thymeleaf + SB Admin 2 + Chart.js 2.9, Spring Security form login (instructor only)

## Architecture rules
- Writes are REST (validated, failures answer with 400/403/409/429 and the common error body); notifications are WebSocket.
- Broadcast only **after commit**: `@TransactionalEventListener(phase = AFTER_COMMIT)`. Never `convertAndSend` inside the transaction.
- Events carry the **whole new state**, never "+1". The server recomputes numbers from the DB. Clients render what they receive.
- Send DTOs, never entities (no participant or session leaks). `participantId` is never exposed to other students.
- Client order on connect and reconnect: **subscribe first, then GET the snapshot**, then apply later events by id (overwrite).
- All topic paths are constants in `live/Topics.java`. `/topic` is for broadcasting, `/app` is unused.
- Security: `/ws/**` is permitAll, participant APIs (`/api/sessions/**`, `/api/questions/**`, `/api/polls/**`) are excluded from CSRF, everything else keeps CSRF. Never `csrf().disable()` as a whole.

## Topics and events
| Topic | Event type | When |
|---|---|---|
| `/topic/sessions/{code}/questions` | CREATED, UPVOTED, ANSWERED, HIDDEN | a question is created or changes |
| `/topic/sessions/{code}/polls` | OPENED, RESULT, CLOSED | a poll starts, gets an answer, or ends |
| `/topic/sessions/{code}/status` | PARTICIPANTS, SESSION_CLOSED | participant count changes, the class ends |

## Business rules (all checked on the server)
| # | Rule | On violation | Enforced by |
|---|---|---|---|
| B1 | Questions, upvotes, polls only while the session is OPEN | 403 | Service |
| B2 | The participant must belong to that session | 403 | Service |
| B3 | Question is 1 to 200 characters; one question per participant per 30 seconds | 400, 429 | Validation, Service |
| B4 | One upvote per participant per question | 409 | DB primary key, `ON CONFLICT DO NOTHING` |
| B5 | Answer only an OPEN poll, one answer per participant | 403, 409 | Service, `ON CONFLICT` |
| B6 | At most one OPEN poll per session | 409 | partial unique index `one_open_poll` |
| B7 | Counts and results are computed from the DB and broadcast | - | Service |
| B8 | HIDDEN questions are excluded from the student view and the snapshot | - | snapshot query |

DB exceptions such as `DataIntegrityViolationException` become 409 in `@RestControllerAdvice`.

## Concurrency (the important part)
- Save an upvote with `INSERT ... ON CONFLICT DO NOTHING`; if 0 rows were inserted, answer 409 `ALREADY_VOTED`.
- Increase the count in the database: `UPDATE question SET upvotes = upvotes + 1 WHERE id = :id`.
- Never read, add one in Java, and `save()`: that loses updates, and `save()` on an entity with a known key is a merge, so duplicates are not detected.
- Expected load test: 30 participants upvote at once -> exactly 30; the same participant 10 times at once -> exactly 1.

## Anonymity rules (README checklist)
1. The join screen tells students not to use their real name or student number in the nickname.
2. Tell students not to put personal information in questions.
3. The instructor can hide an inappropriate question at once.
4. A simple banned-word filter (list in a config file).
5. Participant IDs (UUID) are never shown to other students.
6. Hidden questions are not deleted; only the instructor can see them.
7. Closed sessions are deleted after a fixed retention period (state it in the README).
8. The report shows aggregates only, not per-person data.

## Requirement IDs (use them in commit messages and the prompt log)
L1 open session, L2 join, L3 realtime connection, L4 post question, L5 upvote, L6 answered/hide, L7 reload recovery;
P1 create poll, P2 start/close, P3 answer and live result, P4 close session; S1 anti-flood; R1 class report; R2 challenge (CSV, QR).

## schema.sql
See `docs/schema.sql` (users + 7 tables).
