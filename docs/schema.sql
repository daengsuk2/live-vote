-- YNC Live Class schema: users (instructor login) + 7 tables.
-- "One person, one time" is enforced by PRIMARY KEY / UNIQUE, not by application code.

CREATE TABLE users (
    id        BIGSERIAL PRIMARY KEY,
    email     VARCHAR(100) NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL,
    name      VARCHAR(50)  NOT NULL,
    role      VARCHAR(20)  NOT NULL CHECK (role IN ('INSTRUCTOR'))
);

CREATE TABLE class_session (
    id             BIGSERIAL PRIMARY KEY,
    instructor_id  BIGINT       NOT NULL REFERENCES users(id),
    title          VARCHAR(100) NOT NULL,
    join_code      CHAR(6)      NOT NULL UNIQUE,
    status         VARCHAR(10)  NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED')),
    started_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    ended_at       TIMESTAMPTZ
);
CREATE INDEX idx_class_session_instructor ON class_session(instructor_id);

CREATE TABLE participant (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id  BIGINT      NOT NULL REFERENCES class_session(id),
    nickname    VARCHAR(20) NOT NULL,
    joined_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_participant_session ON participant(session_id);

CREATE TABLE question (
    id              BIGSERIAL PRIMARY KEY,
    session_id      BIGINT       NOT NULL REFERENCES class_session(id),
    participant_id  UUID         NOT NULL REFERENCES participant(id),
    content         VARCHAR(200) NOT NULL CHECK (char_length(content) BETWEEN 1 AND 200),
    upvotes         INT          NOT NULL DEFAULT 0 CHECK (upvotes >= 0),
    status          VARCHAR(10)  NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'ANSWERED', 'HIDDEN')),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    answered_at     TIMESTAMPTZ
);
CREATE INDEX idx_question_board ON question(session_id, status, upvotes DESC);
CREATE INDEX idx_question_participant ON question(participant_id, created_at DESC);

-- B4: the composite primary key makes a second vote from the same participant impossible
CREATE TABLE question_vote (
    question_id     BIGINT      NOT NULL REFERENCES question(id),
    participant_id  UUID        NOT NULL REFERENCES participant(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (question_id, participant_id)
);

CREATE TABLE poll (
    id         BIGSERIAL PRIMARY KEY,
    session_id BIGINT       NOT NULL REFERENCES class_session(id),
    title      VARCHAR(200) NOT NULL,
    status     VARCHAR(10)  NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'OPEN', 'CLOSED')),
    opened_at  TIMESTAMPTZ,
    closed_at  TIMESTAMPTZ
);
-- B6: at most one OPEN poll per session (a second one raises DataIntegrityViolationException -> 409)
CREATE UNIQUE INDEX one_open_poll ON poll(session_id) WHERE status = 'OPEN';

CREATE TABLE poll_option (
    id          BIGSERIAL PRIMARY KEY,
    poll_id     BIGINT       NOT NULL REFERENCES poll(id),
    label       VARCHAR(100) NOT NULL,
    sort_order  INT          NOT NULL,
    UNIQUE (id, poll_id)
);
CREATE INDEX idx_poll_option_poll ON poll_option(poll_id);

-- B5: one answer per participant per poll; the composite FK rejects an option that belongs to another poll
CREATE TABLE poll_answer (
    poll_id         BIGINT      NOT NULL REFERENCES poll(id),
    participant_id  UUID        NOT NULL REFERENCES participant(id),
    option_id       BIGINT      NOT NULL,
    answered_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (poll_id, participant_id),
    FOREIGN KEY (option_id, poll_id) REFERENCES poll_option(id, poll_id)
);
