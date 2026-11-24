import axiosClient from './axiosClient';

export const itemApi = {
  getItems: async (params = {}) => {
    const response = await axiosClient.get('/api/items', { params });
    return response.data;
  },

  getItemById: async (id) => {
    const response = await axiosClient.get(`/api/items/${id}`);
    return response.data;
  },

  createItem: async (formData) => {
    const response = await axiosClient.post('/api/items', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  updateItem: async (id, formData) => {
    const response = await axiosClient.put(`/api/items/${id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  deleteItem: async (id) => {
    const response = await axiosClient.delete(`/api/items/${id}`);
    return response.data;
  },

  updateStatus: async (id, status) => {
    const response = await axiosClient.patch(`/api/items/${id}/status`, { status });
    return response.data;
  },

  getMyItems: async () => {
    const response = await axiosClient.get('/api/items/my-items');
    return response.data;
  },
};
