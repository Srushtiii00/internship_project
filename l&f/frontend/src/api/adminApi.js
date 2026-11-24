import axiosClient from './axiosClient';

export const adminApi = {
  getStats: async () => {
    const response = await axiosClient.get('/api/admin/stats');
    return response.data;
  },

  getUsers: async () => {
    const response = await axiosClient.get('/api/admin/users');
    return response.data;
  },

  deleteUser: async (id) => {
    const response = await axiosClient.delete(`/api/admin/users/${id}`);
    return response.data;
  },

  deleteItem: async (id) => {
    const response = await axiosClient.delete(`/api/admin/items/${id}`);
    return response.data;
  },
};
