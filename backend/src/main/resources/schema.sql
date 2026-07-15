CREATE TABLE IF NOT EXISTS tasks (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(255)  NOT NULL,
    description  CLOB          NOT NULL,
    priority     VARCHAR(20)   NOT NULL DEFAULT 'MEDIUM',
    status       VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
    assignee     VARCHAR(100),
    ai_suggestion CLOB,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
