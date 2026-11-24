import axiosClient from './axiosClient';

export const claimApi = {
  submitClaim: async (claimData) => {
    const response = await axiosClient.post('/api/claims', claimData);
    return response.data;
  },

  getMyClaims: async () => {
    const response = await axiosClient.get('/api/claims/my-claims');
    return response.data;
  },

  getClaimsForItem: async (itemId) => {
    const response = await axiosClient.get(`/api/claims/item/${itemId}`);
    return response.data;
  },

  getClaimsReceived: async () => {
    const response = await axiosClient.get('/api/claims/received');
    return response.data;
  },

  updateClaimStatus: async (claimId, status, remarks = '') => {
    const response = await axiosClient.patch(`/api/claims/${claimId}/status`, {
      status,
      remarks,
    });
    return response.data;
  },
};
