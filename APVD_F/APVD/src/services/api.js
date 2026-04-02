import axios from 'axios';

const DEFAULT_LOCAL_API_URL = 'http://localhost:9000/api';
const DEFAULT_DEPLOYED_API_URL = 'https://academic-progress-visualization-81yi.onrender.com/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || (
  window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
    ? DEFAULT_LOCAL_API_URL
    : DEFAULT_DEPLOYED_API_URL
);

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle response errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth APIs
export const authAPI = {
  register: (userData) => api.post('/auth/register', userData),
  login: (credentials) => api.post('/auth/login', credentials),
  verify: () => api.get('/auth/verify'),
};

// User APIs
export const userAPI = {
  addStudent: (studentData) => api.post('/users/admin/add-student', studentData),
  addFaculty: (facultyData) => api.post('/users/admin/add-faculty', facultyData),
  uploadStudents: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/users/admin/upload-students', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  uploadFaculties: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/users/admin/upload-faculties', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  facultyAddStudent: (studentData) => api.post('/users/faculty/add-student', studentData),
  getUsersByRole: (role) => api.get(`/users/role/${role}`),
  getUserById: (userId) => api.get(`/users/${userId}`),
  getUserByEmail: (email) => api.get(`/users/email/${email}`),
  updateUser: (userId, userData) => api.put(`/users/${userId}`, userData),
  deleteUser: (userId) => api.delete(`/users/${userId}`),
};

// Marks APIs
export const marksAPI = {
  saveMarks: (marksData) => api.post('/marks/save', marksData),
  getStudentMarks: (userId) => api.get(`/marks/${userId}`),
  getSemesterMarks: (userId, semester) => api.get(`/marks/${userId}/semester/${semester}`),
  calculateCGPA: (userId) => api.get(`/marks/${userId}/cgpa`),
  updateSubjectMark: (userId, semester, subjectNumber, mark) =>
    api.put(`/marks/${userId}/semester/${semester}/subject/${subjectNumber}`, { mark }),
  deleteMarks: (marksId) => api.delete(`/marks/${marksId}`),
};

// Attendance APIs
export const attendanceAPI = {
  markAttendance: (attendanceData) => api.post('/attendance/mark', attendanceData),
  markBatchAttendance: (batchData) => api.post('/attendance/mark-batch', batchData),
  getStudentAttendance: (userId) => api.get(`/attendance/${userId}`),
  getAttendanceRange: (userId, startDate, endDate) =>
    api.get(`/attendance/${userId}/range`, { params: { startDate, endDate } }),
  getAttendancePercentage: (userId, startDate, endDate) =>
    api.get(`/attendance/${userId}/percentage`, { params: { startDate, endDate } }),
  getAttendanceByDateAndSession: (date, session) =>
    api.get(`/attendance/date/${date}/session/${session}`),
  getAttendanceByStudents: (userIds) => api.post('/attendance/students', userIds),
  updateAttendance: (attendanceId, data) => api.put(`/attendance/${attendanceId}`, data),
  deleteAttendance: (attendanceId) => api.delete(`/attendance/${attendanceId}`),
};

// Courses APIs (based on backend structure)
export const courseAPI = {
  addCourse: (courseData) => api.post('/courses/add', courseData),
  uploadCourses: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/courses/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  getAllCourses: () => api.get('/courses'),
  getCourseById: (courseId) => api.get(`/courses/${courseId}`),
  deleteCourse: (courseId) => api.delete(`/courses/${courseId}`),
  getCoursesByDepartment: (department) => api.get(`/courses/department/${department}`),
  reassignFacultyCourses: (fromFacultyUserId, toFacultyUserId) =>
    api.put('/courses/reassign-faculty', null, {
      params: { fromFacultyUserId, toFacultyUserId },
    }),
  getCoursesByDepartmentAndSemester: (department, semester) =>
    api.get(`/courses/department/${department}/semester/${semester}`),
  getCoursesBySemester: (semester) => api.get(`/courses/semester/${semester}`),
  getStudentCourses: (userId, semester = 1) => api.get(`/student-courses/${userId}/semester/${semester}`),
  selectCourse: (courseSelectionData) => api.post('/student-courses/select', courseSelectionData),
  dropCourse: (userId, courseId) => api.delete(`/student-courses/${userId}/course/${courseId}`),
  replaceStudentCourse: (studentUserId, oldCourseId, newCourseId, semester) =>
    api.put(`/student-courses/${studentUserId}/course/${oldCourseId}/replace/${newCourseId}`, null, {
      params: { semester },
    }),
  getCourseSelectionStatus: (userId, semester = 1) =>
    api.get(`/student-courses/${userId}/semester/${semester}/status`),
};

// Grades APIs
export const gradesAPI = {
  getStudentGrades: (userId) => api.get(`/grades/student/${userId}`),
  calculateCGPA: (userId) => api.get(`/grades/student/${userId}/cgpa`),
};

// Department APIs
export const departmentAPI = {
  addDepartment: (departmentData) => api.post('/departments/add', departmentData),
  uploadDepartments: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/departments/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  getAllDepartments: () => api.get('/departments/all'),
  getDepartmentById: (departmentId) => api.get(`/departments/${departmentId}`),
  updateDepartment: (departmentId, departmentData) => api.put(`/departments/${departmentId}`, departmentData),
  deleteDepartment: (departmentId) => api.delete(`/departments/${departmentId}`),
};

export default api;
