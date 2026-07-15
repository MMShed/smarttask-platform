import os
from flask import Flask, jsonify, abort
from flask_cors import CORS
from dotenv import load_dotenv
from analytics_service import AnalyticsService

load_dotenv()

app = Flask(__name__)
CORS(app)

db_url = os.getenv(
    'DATABASE_URL',
    'mssql+pyodbc://sa:password@localhost:1433/SmartTask?driver=ODBC+Driver+17+for+SQL+Server'
)
analytics = AnalyticsService(db_url)


@app.get('/health')
def health():
    return jsonify({'status': 'UP', 'service': 'smarttask-analytics'})


@app.get('/summary')
def summary():
    return jsonify(analytics.task_summary())


@app.get('/trend')
def trend():
    days = int(request_arg('days', 30))
    return jsonify(analytics.daily_trend(days))


@app.get('/mttr')
def mttr():
    return jsonify(analytics.mean_time_to_resolve())


@app.get('/priority-distribution')
def priority_dist():
    return jsonify(analytics.priority_distribution())


def request_arg(name, default=None):
    from flask import request
    return request.args.get(name, default)


if __name__ == '__main__':
    port = int(os.getenv('PORT', 5000))
    app.run(host='0.0.0.0', port=port, debug=os.getenv('FLASK_DEBUG', 'false').lower() == 'true')
