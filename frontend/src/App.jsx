import React, { useCallback, useEffect, useState } from 'react';
import {
  AppBar, Box, Container, Divider, Tab, Tabs, Toolbar, Typography
} from '@mui/material';
import BugReportIcon from '@mui/icons-material/BugReport';
import Dashboard    from './components/Dashboard';
import TaskList     from './components/TaskList';
import TaskForm     from './components/TaskForm';
import AIAssistPanel from './components/AIAssistPanel';
import { api } from './services/api';

export default function App() {
  const [tab, setTab]     = useState(0);
  const [tasks, setTasks] = useState([]);

  const loadTasks = useCallback(async () => {
    try {
      const { data } = await api.tasks.getAll();
      setTasks(data);
    } catch (e) {
      console.error('Failed to load tasks', e);
    }
  }, []);

  useEffect(() => { loadTasks(); }, [loadTasks]);

  const handleCreated = (task) => {
    setTasks(prev => [task, ...prev]);
    setTab(1);
  };

  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <BugReportIcon sx={{ mr: 1 }} />
          <Typography variant="h6" fontWeight="bold">SmartTask Platform</Typography>
          <Typography variant="caption" sx={{ ml: 2, opacity: 0.7 }}>
            AI-Powered Enterprise Incident Management
          </Typography>
        </Toolbar>
      </AppBar>

      <Container maxWidth="xl" sx={{ mt: 3, mb: 6 }}>
        <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
          <Tab label="Dashboard" />
          <Tab label="All Tasks" />
          <Tab label="New Task" />
          <Tab label="AI Assist" />
        </Tabs>

        <Divider sx={{ mb: 3 }} />

        {tab === 0 && <Dashboard tasks={tasks} />}
        {tab === 1 && <TaskList tasks={tasks} onRefresh={loadTasks} />}
        {tab === 2 && <Box sx={{ maxWidth: 600 }}><TaskForm onCreated={handleCreated} /></Box>}
        {tab === 3 && <AIAssistPanel />}
      </Container>
    </>
  );
}
