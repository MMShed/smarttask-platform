-- Oracle Stored Procedures / Packages

CREATE OR REPLACE PACKAGE smarttask_pkg AS
    TYPE t_summary_row IS RECORD (
        status        VARCHAR2(20),
        total         NUMBER,
        critical_count NUMBER,
        oldest_open   TIMESTAMP
    );
    TYPE t_summary_tab IS TABLE OF t_summary_row;

    FUNCTION  sp_task_summary          RETURN t_summary_tab PIPELINED;
    PROCEDURE sp_auto_close_stale_tasks(p_days_old IN NUMBER DEFAULT 30, p_rows_affected OUT NUMBER);
    PROCEDURE sp_mttr_report           (p_cursor OUT SYS_REFCURSOR);
END smarttask_pkg;
/

CREATE OR REPLACE PACKAGE BODY smarttask_pkg AS

    FUNCTION sp_task_summary RETURN t_summary_tab PIPELINED IS
        CURSOR c IS
            SELECT status,
                   COUNT(*)                                              AS total,
                   SUM(CASE WHEN priority = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_count,
                   MIN(created_at)                                       AS oldest_open
            FROM tasks
            GROUP BY status
            ORDER BY DECODE(status,'OPEN',1,'IN_PROGRESS',2,'RESOLVED',3,'CLOSED',4);
        r t_summary_row;
    BEGIN
        FOR row IN c LOOP
            r.status         := row.status;
            r.total          := row.total;
            r.critical_count := row.critical_count;
            r.oldest_open    := row.oldest_open;
            PIPE ROW(r);
        END LOOP;
    END;

    PROCEDURE sp_auto_close_stale_tasks(p_days_old IN NUMBER DEFAULT 30, p_rows_affected OUT NUMBER) IS
    BEGIN
        UPDATE tasks
        SET    status     = 'CLOSED',
               updated_at = SYSTIMESTAMP
        WHERE  status     = 'OPEN'
          AND  created_at < SYSTIMESTAMP - p_days_old;
        p_rows_affected := SQL%ROWCOUNT;
        COMMIT;
    END;

    PROCEDURE sp_mttr_report(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR
            SELECT priority,
                   COUNT(*)                                                         AS resolved_count,
                   ROUND(AVG((updated_at - created_at) * 1440))                   AS avg_minutes,
                   ROUND(MIN((updated_at - created_at) * 1440))                   AS min_minutes,
                   ROUND(MAX((updated_at - created_at) * 1440))                   AS max_minutes
            FROM tasks
            WHERE status IN ('RESOLVED','CLOSED')
            GROUP BY priority
            ORDER BY avg_minutes;
    END;

END smarttask_pkg;
/
