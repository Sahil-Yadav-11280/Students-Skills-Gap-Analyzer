import axios from 'axios';

// This is the base URL of your Spring Boot server
const API = axios.create({
    baseURL: 'http://localhost:8082/api/dashboard',
});

export const dashboardService = {
    // 1. Get the list of students for the sidebar
    getAllStudents: async () => {
        const response = await API.get('/students');
        return response.data;
    },

    // 2. Get the massive AI dashboard payload for a specific student
    getStudentDashboard: async (studentId) => {
        const response = await API.get(`/student/${studentId}`);
        return response.data;
    },

    // 3. Add a brand new student
    addStudent: async (name) => {
        const response = await API.post('/students', { name });
        return response.data;
    },

    // 4. Trigger the JSON export download
    exportDashboard: (studentId) => {
        // We use window.open for downloads so the browser handles the file save automatically
        window.open(`http://localhost:8082/api/dashboard/student/${studentId}/export`, '_blank');
    },

    // 5. Get raw history for the editing table
    getStudentHistory: async (studentId) => {
        const response = await API.get(`/student/${studentId}/history`);
        return response.data;
    },

    // 6. Update a specific past attempt
    updateAttempt: async (attemptId, updatedData) => {
        const response = await API.put(`/attempt/${attemptId}`, updatedData);
        return response.data;
    },

    // 7. Log a brand new attempt
    addAttempt: async (studentId, newData) => {
        const response = await API.post(`/student/${studentId}/attempt`, newData);
        return response.data;
    }
};