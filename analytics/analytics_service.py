import pandas as pd
from sqlalchemy import create_engine, text


class AnalyticsService:
    def __init__(self, db_url: str):
        self.engine = create_engine(db_url, pool_pre_ping=True)

    def _query(self, sql: str, **params) -> pd.DataFrame:
        with self.engine.connect() as conn:
            return pd.read_sql(text(sql), conn, params=params)

    def task_summary(self) -> dict:
        df = self._query("SELECT status, COUNT(*) AS count FROM tasks GROUP BY status")
        return df.set_index('status')['count'].to_dict()

    def daily_trend(self, days: int = 30) -> list:
        df = self._query(
            """
            SELECT CAST(created_at AS DATE) AS date, COUNT(*) AS created
            FROM tasks
            WHERE created_at >= DATEADD(DAY, :neg_days, GETDATE())
            GROUP BY CAST(created_at AS DATE)
            ORDER BY date
            """,
            neg_days=-days
        )
        df['date'] = df['date'].astype(str)
        return df.to_dict(orient='records')

    def mean_time_to_resolve(self) -> dict:
        df = self._query(
            """
            SELECT priority,
                   AVG(DATEDIFF(MINUTE, created_at, updated_at)) AS avg_minutes
            FROM tasks
            WHERE status IN ('RESOLVED', 'CLOSED')
            GROUP BY priority
            """
        )
        return df.set_index('priority')['avg_minutes'].to_dict()

    def priority_distribution(self) -> list:
        df = self._query(
            "SELECT priority, COUNT(*) AS count FROM tasks GROUP BY priority ORDER BY count DESC"
        )
        return df.to_dict(orient='records')
