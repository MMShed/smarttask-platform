-- MS SQL Server Stored Procedures
USE SmartTask;
GO

-- Returns task counts grouped by status
CREATE OR ALTER PROCEDURE dbo.sp_task_summary
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        status,
        COUNT(*)                                        AS total,
        SUM(CASE WHEN priority = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_count,
        MIN(created_at)                                 AS oldest_open
    FROM dbo.tasks
    GROUP BY status
    ORDER BY
        CASE status
            WHEN 'OPEN'        THEN 1
            WHEN 'IN_PROGRESS' THEN 2
            WHEN 'RESOLVED'    THEN 3
            WHEN 'CLOSED'      THEN 4
        END;
END;
GO

-- Bulk-resolves tasks older than @days_old that are still OPEN
CREATE OR ALTER PROCEDURE dbo.sp_auto_close_stale_tasks
    @days_old INT = 30,
    @rows_affected INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE dbo.tasks
    SET    status     = 'CLOSED',
           updated_at = SYSDATETIME()
    WHERE  status     = 'OPEN'
      AND  created_at < DATEADD(DAY, -@days_old, SYSDATETIME());

    SET @rows_affected = @@ROWCOUNT;
END;
GO

-- Reports mean time to resolve (MTTR) per priority in minutes
CREATE OR ALTER PROCEDURE dbo.sp_mttr_report
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        priority,
        COUNT(*)                                                   AS resolved_count,
        AVG(DATEDIFF(MINUTE, created_at, updated_at))             AS avg_minutes_to_resolve,
        MIN(DATEDIFF(MINUTE, created_at, updated_at))             AS min_minutes,
        MAX(DATEDIFF(MINUTE, created_at, updated_at))             AS max_minutes
    FROM dbo.tasks
    WHERE status IN ('RESOLVED', 'CLOSED')
    GROUP BY priority
    ORDER BY avg_minutes_to_resolve ASC;
END;
GO
