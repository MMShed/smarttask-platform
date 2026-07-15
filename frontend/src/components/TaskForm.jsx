import React, { useState } from 'react';
import {
  Box, Button, MenuItem, Select, TextField, FormControl,
  InputLabel, Typography, Alert, CircularProgress
} from '@mui/material';
import { api } from '../services/api';

const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
const PROVIDERS  = ['claude', 'bedrock'];

export default function TaskForm({ onCreated }) {
  const [form, setForm]       = useState({ title: '', description: '', priority: '', assignee: '' });
  const [provider, setProvider] = useState('claude');
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState(null);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.tasks.create(form, provider);
      onCreated(data);
      setForm({ title: '', description: '', priority: '', assignee: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create task');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Typography variant="h6">New Incident / Task</Typography>
      {error && <Alert severity="error">{error}</Alert>}

      <TextField label="Title" name="title" value={form.title} onChange={handleChange} required />
      <TextField label="Description" name="description" value={form.description}
                 onChange={handleChange} multiline rows={3} required />

      <FormControl>
        <InputLabel>Priority (AI will decide if blank)</InputLabel>
        <Select name="priority" value={form.priority} onChange={handleChange} label="Priority">
          <MenuItem value="">— Let AI decide —</MenuItem>
          {PRIORITIES.map(p => <MenuItem key={p} value={p}>{p}</MenuItem>)}
        </Select>
      </FormControl>

      <TextField label="Assignee" name="assignee" value={form.assignee} onChange={handleChange} />

      <FormControl>
        <InputLabel>AI Provider</InputLabel>
        <Select value={provider} onChange={e => setProvider(e.target.value)} label="AI Provider">
          {PROVIDERS.map(p => <MenuItem key={p} value={p}>{p === 'claude' ? 'Claude (Anthropic)' : 'Amazon Bedrock'}</MenuItem>)}
        </Select>
      </FormControl>

      <Button type="submit" variant="contained" disabled={loading}
              startIcon={loading ? <CircularProgress size={18} /> : null}>
        {loading ? 'Creating & Triaging…' : 'Create Task'}
      </Button>
    </Box>
  );
}
