-- MS SQL Server Schema
-- Run against: SmartTask database

CREATE TABLE dbo.tasks (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    title        NVARCHAR(255)  NOT NULL,
    description  NVARCHAR(MAX)  NOT NULL,
    priority     NVARCHAR(20)   NOT NULL DEFAULT 'MEDIUM'
                     CONSTRAINT chk_priority CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    status       NVARCHAR(20)   NOT NULL DEFAULT 'OPEN'
                     CONSTRAINT chk_status CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED','CLOSED')),
    assignee     NVARCHAR(100)  NULL,
    ai_suggestion NVARCHAR(MAX) NULL,
    created_at   DATETIME2      NOT NULL DEFAULT SYSDATETIME(),
    updated_at   DATETIME2      NOT NULL DEFAULT SYSDATETIME()
);

CREATE NONCLUSTERED INDEX ix_tasks_status   ON dbo.tasks (status);
CREATE NONCLUSTERED INDEX ix_tasks_priority ON dbo.tasks (priority);
CREATE NONCLUSTERED INDEX ix_tasks_created  ON dbo.tasks (created_at DESC);
