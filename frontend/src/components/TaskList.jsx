import React from 'react';
import {
  Chip, IconButton, Paper, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Tooltip, Typography
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import DeleteIcon from '@mui/icons-material/Delete';
import { api } from '../services/api';

const PRIORITY_COLORS = { LOW: 'default', MEDIUM: 'info', HIGH: 'warning', CRITICAL: 'error' };
const STATUS_COLORS   = { OPEN: 'warning', IN_PROGRESS: 'info', RESOLVED: 'success', CLOSED: 'default' };

export default function TaskList({ tasks, onRefresh }) {
  const handleResolve = async (id) => {
    await api.tasks.resolve(id);
    onRefresh();
  };

  const handleDelete = async (id) => {
    if (window.confirm('Delete this task?')) {
      await api.tasks.delete(id);
      onRefresh();
    }
  };

  if (!tasks.length) return <Typography color="text.secondary">No tasks found.</Typography>;

  return (
    <TableContainer component={Paper} elevation={2}>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>ID</TableCell>
            <TableCell>Title</TableCell>
            <TableCell>Priority</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Assignee</TableCell>
            <TableCell>AI Suggestion</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {tasks.map(task => (
            <TableRow key={task.id} hover>
              <TableCell>{task.id}</TableCell>
              <TableCell>{task.title}</TableCell>
              <TableCell>
                <Chip label={task.priority} color={PRIORITY_COLORS[task.priority]} size="small" />
              </TableCell>
              <TableCell>
                <Chip label={task.status} color={STATUS_COLORS[task.status]} size="small" />
              </TableCell>
              <TableCell>{task.assignee || '—'}</TableCell>
              <TableCell sx={{ maxWidth: 240, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                <Tooltip title={task.aiSuggestion || ''}>
                  <span>{task.aiSuggestion || '—'}</span>
                </Tooltip>
              </TableCell>
              <TableCell>
                {task.status !== 'RESOLVED' && task.status !== 'CLOSED' && (
                  <Tooltip title="Mark Resolved">
                    <IconButton size="small" color="success" onClick={() => handleResolve(task.id)}>
                      <CheckCircleIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                )}
                <Tooltip title="Delete">
                  <IconButton size="small" color="error" onClick={() => handleDelete(task.id)}>
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
