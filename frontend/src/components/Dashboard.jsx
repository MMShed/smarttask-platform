import React from 'react';
import { Box, Grid, Paper, Typography } from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';

const PRIORITY_COLOR = { LOW: '#4caf50', MEDIUM: '#2196f3', HIGH: '#ff9800', CRITICAL: '#f44336' };

export default function Dashboard({ tasks }) {
  const byStatus = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'].map(s => ({
    name: s, count: tasks.filter(t => t.status === s).length,
  }));

  const byPriority = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map(p => ({
    name: p, count: tasks.filter(t => t.priority === p).length,
  }));

  const Stat = ({ label, value, color }) => (
    <Paper elevation={2} sx={{ p: 2, textAlign: 'center' }}>
      <Typography variant="h4" color={color} fontWeight="bold">{value}</Typography>
      <Typography variant="body2" color="text.secondary">{label}</Typography>
    </Paper>
  );

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      <Grid container spacing={2}>
        <Grid item xs={6} sm={3}><Stat label="Total"      value={tasks.length}                                      color="text.primary" /></Grid>
        <Grid item xs={6} sm={3}><Stat label="Open"       value={tasks.filter(t => t.status === 'OPEN').length}     color="warning.main" /></Grid>
        <Grid item xs={6} sm={3}><Stat label="Resolved"   value={tasks.filter(t => t.status === 'RESOLVED').length} color="success.main" /></Grid>
        <Grid item xs={6} sm={3}><Stat label="Critical"   value={tasks.filter(t => t.priority === 'CRITICAL').length} color="error.main" /></Grid>
      </Grid>

      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Paper elevation={2} sx={{ p: 2 }}>
            <Typography variant="subtitle1" mb={1}>Tasks by Status</Typography>
            <ResponsiveContainer width="100%" height={180}>
              <BarChart data={byStatus}>
                <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="count" fill="#1976d2" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
        <Grid item xs={12} md={6}>
          <Paper elevation={2} sx={{ p: 2 }}>
            <Typography variant="subtitle1" mb={1}>Tasks by Priority</Typography>
            <ResponsiveContainer width="100%" height={180}>
              <BarChart data={byPriority}>
                <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="count">
                  {byPriority.map(entry => (
                    <Cell key={entry.name} fill={PRIORITY_COLOR[entry.name]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
