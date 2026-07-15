const express     = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const rateLimit   = require('express-rate-limit');
const helmet      = require('helmet');
const cors        = require('cors');
const morgan      = require('morgan');

const app = express();
const PORT = process.env.PORT || 3001;

const BACKEND_URL  = process.env.BACKEND_URL  || 'http://localhost:8080';
const ANALYTICS_URL = process.env.ANALYTICS_URL || 'http://localhost:5000';

app.use(helmet());
app.use(cors({ origin: process.env.ALLOWED_ORIGIN || 'http://localhost:3000' }));
app.use(morgan('combined'));

const apiLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 100,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Too many requests — please try again later.' },
});
app.use('/api/', apiLimiter);

// Route /api/tasks and /api/ai -> Spring Boot backend
app.use('/api', createProxyMiddleware({
  target: BACKEND_URL,
  changeOrigin: true,
  on: {
    error: (err, req, res) => {
      console.error('Backend proxy error:', err.message);
      res.status(502).json({ error: 'Backend unavailable' });
    },
  },
}));

// Route /analytics -> Python analytics service
app.use('/analytics', createProxyMiddleware({
  target: ANALYTICS_URL,
  changeOrigin: true,
  pathRewrite: { '^/analytics': '' },
}));

app.get('/health', (_, res) => res.json({ status: 'UP', service: 'smarttask-gateway' }));

app.listen(PORT, () => console.log(`Gateway listening on port ${PORT}`));
