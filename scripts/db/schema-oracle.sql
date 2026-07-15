-- Oracle Schema
CREATE SEQUENCE tasks_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE tasks (
    id            NUMBER         DEFAULT tasks_seq.NEXTVAL PRIMARY KEY,
    title         VARCHAR2(255)  NOT NULL,
    description   CLOB           NOT NULL,
    priority      VARCHAR2(20)   DEFAULT 'MEDIUM'
                      CONSTRAINT chk_priority CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    status        VARCHAR2(20)   DEFAULT 'OPEN'
                      CONSTRAINT chk_status CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED','CLOSED')),
    assignee      VARCHAR2(100),
    ai_suggestion CLOB,
    created_at    TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at    TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE INDEX ix_tasks_status   ON tasks (status);
CREATE INDEX ix_tasks_priority ON tasks (priority);
CREATE INDEX ix_tasks_created  ON tasks (created_at DESC);
