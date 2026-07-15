import axios from 'axios';

const BASE = '/api';

export const api = {
  tasks: {
    getAll:   ()          => axios.get(`${BASE}/tasks`),
    getById:  (id)        => axios.get(`${BASE}/tasks/${id}`),
    create:   (data, provider = 'claude') =>
                             axios.post(`${BASE}/tasks?aiProvider=${provider}`, data),
    update:   (id, data)  => axios.put(`${BASE}/tasks/${id}`, data),
    resolve:  (id)        => axios.patch(`${BASE}/tasks/${id}/resolve`),
    delete:   (id)        => axios.delete(`${BASE}/tasks/${id}`),
    summary:  ()          => axios.get(`${BASE}/tasks/summary`),
  },
  ai: {
    triage:   (data)      => axios.post(`${BASE}/ai/triage`, data),
    compare:  (data)      => axios.post(`${BASE}/ai/triage/compare`, data),
  },
};
