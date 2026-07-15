import React, { useState } from 'react';
import {
  Box, Button, Card, CardContent, Chip, CircularProgress,
  Divider, TextField, Typography
} from '@mui/material';
import { api } from '../services/api';

export default function AIAssistPanel() {
  const [title, setTitle]         = useState('');
  const [description, setDesc]    = useState('');
  const [responses, setResponses] = useState([]);
  const [loading, setLoading]     = useState(false);

  const handleCompare = async () => {
    setLoading(true);
    try {
      const { data } = await api.ai.compare({ title, description, provider: 'claude' });
      setResponses(data);
    } catch (e) {
      setResponses([{ provider: 'error', suggestion: e.message }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Typography variant="h6">AI Triage Comparison</Typography>
      <Typography variant="body2" color="text.secondary">
        Submit a task description to compare Claude (Anthropic) vs Amazon Bedrock responses side-by-side.
      </Typography>

      <TextField label="Title" value={title} onChange={e => setTitle(e.target.value)} />
      <TextField label="Description" value={description} onChange={e => setDesc(e.target.value)}
                 multiline rows={3} />

      <Button variant="outlined" onClick={handleCompare} disabled={loading || !title || !description}
              startIcon={loading ? <CircularProgress size={18} /> : null}>
        {loading ? 'Comparing…' : 'Compare Claude vs Bedrock'}
      </Button>

      {responses.length > 0 && (
        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
          {responses.map((r, i) => (
            <Card key={i} sx={{ flex: 1, minWidth: 260 }} elevation={3}>
              <CardContent>
                <Typography variant="subtitle1" fontWeight="bold">{r.provider?.toUpperCase()}</Typography>
                <Divider sx={{ my: 1 }} />
                <Box sx={{ display: 'flex', gap: 1, mb: 1 }}>
                  <Chip label={`Priority: ${r.priority}`} size="small" color="warning" />
                  <Chip label={`Confidence: ${(r.confidence * 100).toFixed(0)}%`} size="small" />
                </Box>
                <Typography variant="body2"><b>Category:</b> {r.category}</Typography>
                <Typography variant="body2" sx={{ mt: 1 }}>{r.suggestion}</Typography>
              </CardContent>
            </Card>
          ))}
        </Box>
      )}
    </Box>
  );
}
