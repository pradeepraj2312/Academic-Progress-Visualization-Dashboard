import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useAuth } from '../services/AuthContext';
import { attendanceAPI, marksAPI, userAPI, courseAPI } from '../services/api';
import { FiUsers, FiBarChart2, FiPlus, FiTrash2, FiEye, FiDownload, FiEdit2, FiX } from 'react-icons/fi';
import { MdCheckCircle, MdCancel } from 'react-icons/md';
import { Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import '../styles/Dashboard.css';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const FacultyDashboard = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('attendance');
  const [students, setStudents] = useState([]);
  const [selectedStudent, setSelectedStudent] = useState(null);
  const [studentMarks, setStudentMarks] = useState({});
  const [studentSelectedCourses, setStudentSelectedCourses] = useState([]);
  const [availableSemesterCourses, setAvailableSemesterCourses] = useState([]);
  const [courseSelectionStatus, setCourseSelectionStatus] = useState(null);
  const [courseSemester, setCourseSemester] = useState(1);
  const [replacementCourseIds, setReplacementCourseIds] = useState({});
  const [facultyDepartment, setFacultyDepartment] = useState(user?.department || '');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [newStudent, setNewStudent] = useState({
    userId: '',
    username: '',
    userEmail: '',
    userPassword: '',
    mobile: '',
    enrollmentNumber: '',
    department: user?.department || '',
    yearOfStudying: 1,
  });
  const [markUpdate, setMarkUpdate] = useState({
    semester: 1,
    subject1Mark: '',
    subject2Mark: '',
    subject3Mark: '',
    subject4Mark: '',
    subject5Mark: '',
    subject6Mark: '',
  });
  const [attendanceFilters, setAttendanceFilters] = useState({
    year: '',
    department: user?.department || '',
  });
  const [attendanceMarkingData, setAttendanceMarkingData] = useState({
    date: new Date().toISOString().split('T')[0],
    session: 'FN',
    remarks: 'Marked by Faculty',
  });
  const [selectedStudentsForAttendance, setSelectedStudentsForAttendance] = useState(new Set());
  const [searchQuery, setSearchQuery] = useState('');
  const [attendanceTableData, setAttendanceTableData] = useState([]);
  const [attendanceSearchQuery, setAttendanceSearchQuery] = useState('');
  const [showMarkAttendance, setShowMarkAttendance] = useState(false);
  const [editingAttendanceId, setEditingAttendanceId] = useState(null);
  const [toast, setToast] = useState({ message: '', visible: false });
  const studentsLoadKeyRef = useRef('');
  const profileLoadUserRef = useRef('');

  const showToast = (message) => {
    setToast({ message, visible: true });
    const timer = setTimeout(() => {
      setToast({ message: '', visible: false });
    }, 3000);
    return () => clearTimeout(timer);
  };

  useEffect(() => {
    const loadFacultyDepartment = async () => {
      if (!user?.userId) return;
      if (profileLoadUserRef.current === user.userId) return;

      profileLoadUserRef.current = user.userId;
      try {
        const response = await userAPI.getUserById(user.userId);
        const department = response.data?.department || user?.department || '';
        setFacultyDepartment(department);
        setAttendanceFilters((prev) => ({ ...prev, department }));
        setNewStudent((prev) => ({ ...prev, department }));
      } catch {
        setFacultyDepartment(user?.department || '');
      }
    };

    loadFacultyDepartment();
  }, [user]);

  const fetchAttendanceData = useCallback(async (departmentStudents = []) => {
    try {
      if (departmentStudents.length > 0) {
        const userIds = departmentStudents.map(s => s.userId);
        const attendanceRes = await attendanceAPI.getAttendanceByStudents(userIds);
        setAttendanceTableData(attendanceRes.data || []);
      } else {
        setAttendanceTableData([]);
      }
    } catch (err) {
      console.error('Failed to fetch attendance data:', err);
    }
  }, []);

  const fetchStudents = useCallback(async () => {
    setLoading(true);
    try {
      const response = await userAPI.getUsersByRole('STUDENT');
      const allStudents = response.data || [];
      const activeDepartment = facultyDepartment || user?.department || '';
      const departmentStudents = activeDepartment
        ? allStudents.filter((s) => s.department === activeDepartment)
        : [];
      setStudents(departmentStudents);
      await fetchAttendanceData(departmentStudents);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch students');
    } finally {
      setLoading(false);
    }
  }, [user, facultyDepartment, fetchAttendanceData]);

  useEffect(() => {
    if (!user?.userId) return;
    const activeDepartment = facultyDepartment || user?.department || '';
    const loadKey = `${user.userId}:${activeDepartment}`;
    if (studentsLoadKeyRef.current === loadKey) return;

    studentsLoadKeyRef.current = loadKey;
    fetchStudents();
  }, [user, facultyDepartment, fetchStudents]);



  const getFilteredStudents = () => {
    let filtered = students.filter((student) => {
      if (attendanceFilters.year && student.yearOfStudying !== parseInt(attendanceFilters.year)) {
        return false;
      }
      return true;
    });

    // Apply search query
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter((student) =>
        student.username.toLowerCase().includes(query) ||
        student.userEmail.toLowerCase().includes(query) ||
        (student.mobile || '').toLowerCase().includes(query) ||
        student.userId.toLowerCase().includes(query) ||
        (student.enrollmentNumber || '').toLowerCase().includes(query) ||
        (student.department || '').toLowerCase().includes(query) ||
        String(student.yearOfStudying || '').includes(query)
      );
    }

    return filtered;
  };

  const getFilteredAttendanceData = () => {
    const grouped = new Map();

    attendanceTableData.forEach((record) => {
      const dateKey =
        typeof record.attendanceDate === 'string'
          ? record.attendanceDate
          : new Date(record.attendanceDate).toISOString().split('T')[0];
      const key = `${record.userId}__${dateKey}`;

      if (!grouped.has(key)) {
        grouped.set(key, {
          key,
          userId: record.userId,
          username: record.username,
          userEmail: record.userEmail,
          enrollmentNumber: record.enrollmentNumber,
          department: record.department,
          yearOfStudying: record.yearOfStudying,
          attendanceDate: dateKey,
          fn: null,
          an: null,
          remarks: [],
        });
      }

      const row = grouped.get(key);
      if ((record.session || '').toUpperCase() === 'FN') {
        row.fn = record;
      } else if ((record.session || '').toUpperCase() === 'AN') {
        row.an = record;
      }

      if (record.remarks) {
        row.remarks.push(record.remarks);
      }
    });

    const groupedList = Array.from(grouped.values()).sort(
      (a, b) => new Date(b.attendanceDate) - new Date(a.attendanceDate)
    );

    if (!attendanceSearchQuery.trim()) {
      return groupedList;
    }

    const query = attendanceSearchQuery.toLowerCase();
    return groupedList.filter((record) => {
      const fnStatus = record.fn?.status?.toLowerCase() || '';
      const anStatus = record.an?.status?.toLowerCase() || '';
      return (
        record.username.toLowerCase().includes(query) ||
        record.userEmail.toLowerCase().includes(query) ||
        record.userId.toLowerCase().includes(query) ||
        (record.enrollmentNumber || '').toLowerCase().includes(query) ||
        (record.department || '').toLowerCase().includes(query) ||
        String(record.yearOfStudying || '').includes(query) ||
        record.attendanceDate?.toString().includes(query) ||
        fnStatus.includes(query) ||
        anStatus.includes(query)
      );
    });
  };

  const getTodaysAttendanceStatus = (studentId) => {
    const today = attendanceMarkingData.date;
    const session = attendanceMarkingData.session;
    
    const todayRecord = attendanceTableData.find(
      record => {
        const recordDate = typeof record.attendanceDate === 'string' 
          ? record.attendanceDate 
          : new Date(record.attendanceDate).toISOString().split('T')[0];
        
        return (
          record.userId === studentId &&
          recordDate === today &&
          record.session === session
        );
      }
    );
    
    return todayRecord;
  };

  const handleEditAttendance = async (attendanceId, status) => {
    try {
      setLoading(true);
      const attendanceRecord = attendanceTableData.find((record) => record.id === attendanceId);

      if (!attendanceRecord) {
        setError('Attendance record not found');
        setLoading(false);
        return;
      }

      await attendanceAPI.updateAttendance(attendanceId, {
        userId: attendanceRecord.userId,
        status: status,
        session: attendanceRecord.session,
        attendanceDate: attendanceRecord.attendanceDate,
        remarks: attendanceRecord.remarks || attendanceMarkingData.remarks,
      });

      showToast(`Attendance updated to ${status}`);
      setEditingAttendanceId(null);
      fetchAttendanceData(students); // Refresh attendance table
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update attendance');
    } finally {
      setLoading(false);
    }
  };

  const handleStudentToggle = (studentId) => {
    const newSet = new Set(selectedStudentsForAttendance);
    if (newSet.has(studentId)) {
      newSet.delete(studentId);
    } else {
      newSet.add(studentId);
    }
    setSelectedStudentsForAttendance(newSet);
  };

  const handleSelectAllFiltered = () => {
    const filteredStudents = getFilteredStudents();
    if (selectedStudentsForAttendance.size === filteredStudents.length) {
      setSelectedStudentsForAttendance(new Set());
    } else {
      setSelectedStudentsForAttendance(new Set(filteredStudents.map((s) => s.userId)));
    }
  };

  const handleBatchMarkAttendance = async (status) => {
    if (selectedStudentsForAttendance.size === 0) {
      setError('Please select at least one student');
      return;
    }
    if (!attendanceMarkingData.date) {
      setError('Please select a date');
      return;
    }
    if (!attendanceMarkingData.session) {
      setError('Please select a session');
      return;
    }

    try {
      setLoading(true);
      await attendanceAPI.markBatchAttendance({
        attendanceDate: attendanceMarkingData.date,
        session: attendanceMarkingData.session,
        studentIds: Array.from(selectedStudentsForAttendance),
        status: status,
        remarks: attendanceMarkingData.remarks,
      });

      showToast('Attendance marked successfully for ' + selectedStudentsForAttendance.size + ' students!');
      setSelectedStudentsForAttendance(new Set());
      setError('');
      fetchAttendanceData(students); // Refresh attendance table
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to mark attendance');
    } finally {
      setLoading(false);
    }
  };

  const handleMarkIndividualAttendance = async (studentId, status) => {
    if (!attendanceMarkingData.date || !attendanceMarkingData.session) {
      setError('Please select date and session');
      return;
    }

    try {
      setLoading(true);
      await attendanceAPI.markBatchAttendance({
        attendanceDate: attendanceMarkingData.date,
        session: attendanceMarkingData.session,
        studentIds: [studentId],
        status: status,
        remarks: attendanceMarkingData.remarks,
      });

      showToast(`Marked ${status.toLowerCase()} successfully!`);
      fetchAttendanceData(students); // Refresh attendance table
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to mark attendance');
    } finally {
      setLoading(false);
    }
  }

  const handleStudentSelect = async (student) => {
    setSelectedStudent(student);
    setError('');

    try {
      const marksRes = await marksAPI.getStudentMarks(student.userId);

      setStudentMarks(marksRes.data || {});
      setMarkUpdate((prev) => ({
        ...prev,
        semester: 1,
      }));
    } catch (error) {
      console.error('Error fetching student data:', error);
      setError('Failed to fetch student data');
    }
  };

  const loadStudentCourseManagementData = useCallback(async () => {
    if (!selectedStudent?.userId) return;

    try {
      const [selectedRes, allCoursesRes, statusRes] = await Promise.all([
        courseAPI.getStudentCourses(selectedStudent.userId, courseSemester),
        courseAPI.getCoursesBySemester(courseSemester),
        courseAPI.getCourseSelectionStatus(selectedStudent.userId, courseSemester),
      ]);

      setStudentSelectedCourses(selectedRes.data || []);
      setAvailableSemesterCourses(allCoursesRes.data || []);
      setCourseSelectionStatus(statusRes.data || null);
      setReplacementCourseIds({});
    } catch (err) {
      console.error('Failed to load student course management data:', err);
      setError(err.response?.data?.message || 'Failed to load student courses');
      setStudentSelectedCourses([]);
      setAvailableSemesterCourses([]);
      setCourseSelectionStatus(null);
    }
  }, [selectedStudent, courseSemester]);

  useEffect(() => {
    if (activeTab === 'courses' && selectedStudent?.userId) {
      loadStudentCourseManagementData();
    }
  }, [activeTab, selectedStudent, courseSemester, loadStudentCourseManagementData]);

  const handleReplaceCourse = async (oldCourseId) => {
    const newCourseId = parseInt(replacementCourseIds[oldCourseId], 10);

    if (!selectedStudent?.userId || !newCourseId) {
      setError('Please select a replacement course');
      return;
    }

    try {
      await courseAPI.replaceStudentCourse(
        selectedStudent.userId,
        oldCourseId,
        newCourseId,
        courseSemester
      );
      showToast('Student course updated successfully!');
      setError('');
      loadStudentCourseManagementData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to replace course');
    }
  };



  const handleSaveMarks = async () => {
    if (!selectedStudent) return;

    try {
      const markData = {
        userId: selectedStudent.userId,
        semester: markUpdate.semester,
        subject1Mark: parseInt(markUpdate.subject1Mark) || 0,
        subject2Mark: parseInt(markUpdate.subject2Mark) || 0,
        subject3Mark: parseInt(markUpdate.subject3Mark) || 0,
        subject4Mark: parseInt(markUpdate.subject4Mark) || 0,
        subject5Mark: parseInt(markUpdate.subject5Mark) || 0,
        subject6Mark: parseInt(markUpdate.subject6Mark) || 0,
      };

      await marksAPI.saveMarks(markData);
      setError('');
      showToast('Marks saved successfully!');
      handleStudentSelect(selectedStudent);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save marks');
    }
  };

  const handleAddStudent = async () => {
    try {
      if (
        !newStudent.userId ||
        !newStudent.username ||
        !newStudent.userEmail ||
        !newStudent.userPassword ||
        !newStudent.mobile
      ) {
        setError('Please fill in all required fields');
        return;
      }

      if (!(facultyDepartment || user?.department || newStudent.department)) {
        setError('Faculty department is missing. Please re-login and try again.');
        return;
      }

      await userAPI.facultyAddStudent({
        ...newStudent,
        department: facultyDepartment || user?.department || newStudent.department,
      });
      setNewStudent({
        userId: '',
        username: '',
        userEmail: '',
        userPassword: '',
        mobile: '',
        enrollmentNumber: '',
        department: facultyDepartment || user?.department || '',
        yearOfStudying: 1,
      });

      showToast('Student added successfully!');
      fetchStudents();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add student');
    }
  };




  return (
    <div className="dashboard-container">
      {toast.visible && (
        <div className="toast-notification">
          {toast.message}
        </div>
      )}
      <div className="dashboard-header">
        <h1>Faculty Dashboard</h1>
        <p>Welcome, {user?.username}!</p>
      </div>

      <div className="dashboard-tabs">
        <button
          className={`tab-button ${activeTab === 'students' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('students');
            setError('');
          }}
        >
          <FiUsers style={{ marginRight: '6px' }} />
          Students
        </button>
        <button
          className={`tab-button ${activeTab === 'attendance' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('attendance');
            setError('');
          }}
        >
          <FiBarChart2 style={{ marginRight: '6px' }} />
          Attendance
        </button>
        <button
          className={`tab-button ${activeTab === 'results' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('results');
            setError('');
          }}
        >
          <FiBarChart2 style={{ marginRight: '6px' }} />
          Results
        </button>
        <button
          className={`tab-button ${activeTab === 'courses' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('courses');
            setError('');
          }}
        >
          <FiDownload style={{ marginRight: '6px' }} />
          Courses
        </button>
        <button
          className={`tab-button ${activeTab === 'addStudent' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('addStudent');
            setError('');
          }}
        >
          <FiPlus style={{ marginRight: '6px' }} />
          Add Student
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="tab-content">
        {/* Attendance Tab */}
        {activeTab === 'attendance' && (
          <div className="tab-panel">
            <div className="attendance-controls">
              <div className="section-header">
                <h3><FiBarChart2 style={{ marginRight: '8px' }} />Attendance Management</h3>
                <button 
                  className="btn btn-primary"
                  onClick={() => setShowMarkAttendance(!showMarkAttendance)}
                >
                  {showMarkAttendance ? (
                    <>
                      <FiEye style={{ marginRight: '4px' }} />
                      View All Attendance
                    </>
                  ) : (
                    <>
                      <FiEdit2 style={{ marginRight: '4px' }} />
                      Mark Attendance
                    </>
                  )}
                </button>
              </div>
              
              {showMarkAttendance ? (
                <>
                  <div className="filter-section">
                    <h4>Filter Students</h4>
                    <div className="filters-grid">
                      <div className="filter-group">
                        <label>Department:</label>
                        <input type="text" value={user?.department || ''} disabled />
                      </div>
                      
                      <div className="filter-group">
                        <label>Year:</label>
                        <select
                          value={attendanceFilters.year}
                          onChange={(e) => setAttendanceFilters({ ...attendanceFilters, year: e.target.value })}
                        >
                          <option value="">All Years</option>
                          <option value="1">Year 1</option>
                          <option value="2">Year 2</option>
                          <option value="3">Year 3</option>
                          <option value="4">Year 4</option>
                        </select>
                      </div>
                    </div>
                  </div>

                  <div className="attendance-marking-section">
                    <h4>Session Details</h4>
                    <div className="marking-controls">
                      <div className="control-group">
                        <label>Date:</label>
                        <input
                          type="date"
                          value={attendanceMarkingData.date}
                          onChange={(e) =>
                            setAttendanceMarkingData({ ...attendanceMarkingData, date: e.target.value })
                          }
                        />
                      </div>

                      <div className="control-group">
                        <label>Session:</label>
                        <select
                          value={attendanceMarkingData.session}
                          onChange={(e) =>
                            setAttendanceMarkingData({ ...attendanceMarkingData, session: e.target.value })
                          }
                        >
                          <option value="FN">Forenoon (FN)</option>
                          <option value="AN">Afternoon (AN)</option>
                        </select>
                      </div>

                      <div className="control-group">
                        <label>Remarks (Optional):</label>
                        <input
                          type="text"
                          placeholder="e.g., Marked by Faculty"
                          value={attendanceMarkingData.remarks}
                          onChange={(e) =>
                            setAttendanceMarkingData({ ...attendanceMarkingData, remarks: e.target.value })
                          }
                        />
                      </div>
                    </div>
                  </div>

                  {error && <div className="error-message">{error}</div>}

                  <div className="students-list-container">
                    <div className="list-header">
                      <h3><FiEdit2 style={{ marginRight: '8px' }} />Mark Attendance</h3>
                      <div className="search-bar">
                        <input
                          type="text"
                          placeholder="Search by name, email, mobile, roll no, department, year, or ID..."
                          value={searchQuery}
                          onChange={(e) => setSearchQuery(e.target.value)}
                          className="search-input"
                        />
                      </div>
                      <button
                        className="btn btn-secondary btn-sm"
                        onClick={handleSelectAllFiltered}
                      >
                        {selectedStudentsForAttendance.size === getFilteredStudents().length && getFilteredStudents().length > 0
                          ? 'Deselect All'
                          : 'Select All'}
                      </button>
                      <span className="selection-counter">
                        Selected: {selectedStudentsForAttendance.size} / {getFilteredStudents().length}
                      </span>
                    </div>

                    {getFilteredStudents().length > 0 ? (
                      <table className="data-table">
                        <thead>
                          <tr>
                            <th style={{ width: '40px' }}>
                              <input
                                type="checkbox"
                                checked={
                                  selectedStudentsForAttendance.size === getFilteredStudents().length &&
                                  getFilteredStudents().length > 0
                                }
                                onChange={handleSelectAllFiltered}
                              />
                            </th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Mobile</th>
                            <th>Roll Number</th>
                            <th>Department</th>
                            <th>Year</th>
                            <th>User ID</th>
                            <th style={{ width: '200px' }}>Actions</th>
                          </tr>
                        </thead>
                        <tbody>
                          {getFilteredStudents().map((student) => {
                            const todayStatus = getTodaysAttendanceStatus(student.userId);
                            return (
                              <tr key={student.userId} className={selectedStudentsForAttendance.has(student.userId) ? 'selected' : ''}>
                                <td>
                                  <input
                                    type="checkbox"
                                    checked={selectedStudentsForAttendance.has(student.userId)}
                                    onChange={() => handleStudentToggle(student.userId)}
                                    onClick={(e) => e.stopPropagation()}
                                  />
                                </td>
                                <td><strong>{student.username}</strong></td>
                                <td>{student.userEmail}</td>
                                <td>{student.mobile || '-'}</td>
                                <td>{student.enrollmentNumber || '-'}</td>
                                <td>{student.department || '-'}</td>
                                <td>{student.yearOfStudying ?? '-'}</td>
                                <td>{student.userId}</td>
                                <td>
                                  <div className="action-buttons-inline">
                                    {todayStatus ? (
                                      <div className="status-display">
                                        <span className={`status-badge ${todayStatus.status.toLowerCase()}`}>
                                          {todayStatus.status === 'PRESENT' ? (
                                            <>
                                              <MdCheckCircle style={{ marginRight: '4px' }} />
                                              Present
                                            </>
                                          ) : (
                                            <>
                                              <MdCancel style={{ marginRight: '4px' }} />
                                              Absent
                                            </>
                                          )}
                                        </span>
                                        <button 
                                          className="btn btn-edit btn-icon-only"
                                          onClick={() => setEditingAttendanceId(editingAttendanceId === todayStatus.id ? null : todayStatus.id)}
                                          title={editingAttendanceId === todayStatus.id ? 'Cancel' : 'Edit'}
                                        >
                                          {editingAttendanceId === todayStatus.id ? (
                                            <FiX size={18} />
                                          ) : (
                                            <FiEdit2 size={18} />
                                          )}
                                        </button>
                                        {editingAttendanceId === todayStatus.id && (
                                          <div className="edit-buttons">
                                            <button 
                                              className="btn btn-present btn-xs"
                                              onClick={() => handleEditAttendance(todayStatus.id, 'PRESENT')}
                                              disabled={loading}
                                            >
                                              <MdCheckCircle /> Present
                                            </button>
                                            <button 
                                              className="btn btn-absent btn-xs"
                                              onClick={() => handleEditAttendance(todayStatus.id, 'ABSENT')}
                                              disabled={loading}
                                            >
                                              <MdCancel /> Absent
                                            </button>
                                          </div>
                                        )}
                                      </div>
                                    ) : (
                                      <>
                                        <button 
                                          className="btn btn-present btn-sm"
                                          onClick={() => handleMarkIndividualAttendance(student.userId, 'PRESENT')}
                                          disabled={loading}
                                        >
                                          <MdCheckCircle /> Present
                                        </button>
                                        <button 
                                          className="btn btn-absent btn-sm"
                                          onClick={() => handleMarkIndividualAttendance(student.userId, 'ABSENT')}
                                          disabled={loading}
                                        >
                                          <MdCancel /> Absent
                                        </button>
                                      </>
                                    )}
                                  </div>
                                </td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    ) : (
                      <p className="no-data-message">No students found matching the filters</p>
                    )}
                  </div>

                  {selectedStudentsForAttendance.size > 0 && (
                    <div className="action-buttons batch-buttons">
                      <button
                        className="btn btn-present btn-with-icon"
                        onClick={() => handleBatchMarkAttendance('PRESENT')}
                        disabled={loading}
                      >
                        <FiPlus /> Mark {selectedStudentsForAttendance.size} as Present
                      </button>
                      <button
                        className="btn btn-absent btn-with-icon"
                        onClick={() => handleBatchMarkAttendance('ABSENT')}
                        disabled={loading}
                      >
                        <FiTrash2 /> Mark {selectedStudentsForAttendance.size} as Absent
                      </button>
                    </div>
                  )}
                </>
              ) : (
                <>
                  <div className="attendance-table-section">
                    <div className="list-header">
                      <h3><FiBarChart2 style={{ marginRight: '8px' }} />All Attendance Records</h3>
                      <div className="search-bar">
                        <input
                          type="text"
                          placeholder="Search by name, email, ID, or date..."
                          value={attendanceSearchQuery}
                          onChange={(e) => setAttendanceSearchQuery(e.target.value)}
                          className="search-input"
                        />
                      </div>
                    </div>

                    {getFilteredAttendanceData().length > 0 ? (
                      <div className="table-wrapper">
                        <table className="data-table">
                          <thead>
                            <tr>
                              <th>Student Name</th>
                              <th>Roll Number</th>
                              <th>User ID</th>
                              <th>Email</th>
                              <th>Department</th>
                              <th>Year</th>
                              <th>Date</th>
                              <th>FN</th>
                              <th>AN</th>
                              <th>Remarks</th>
                            </tr>
                          </thead>
                          <tbody>
                            {getFilteredAttendanceData().map((record) => (
                              <tr key={record.key}>
                                <td><strong>{record.username}</strong></td>
                                <td>{record.enrollmentNumber || '-'}</td>
                                <td>{record.userId}</td>
                                <td>{record.userEmail}</td>
                                <td>{record.department || '-'}</td>
                                <td>{record.yearOfStudying ?? '-'}</td>
                                <td>{new Date(record.attendanceDate).toLocaleDateString()}</td>
                                <td>
                                  {record.fn ? (
                                    <span className={`badge badge-${record.fn.status.toLowerCase()}`}>
                                      {record.fn.status}
                                    </span>
                                  ) : (
                                    '-'
                                  )}
                                </td>
                                <td>
                                  {record.an ? (
                                    <span className={`badge badge-${record.an.status.toLowerCase()}`}>
                                      {record.an.status}
                                    </span>
                                  ) : (
                                    '-'
                                  )}
                                </td>
                                <td>{record.remarks.length > 0 ? [...new Set(record.remarks)].join(', ') : '-'}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    ) : (
                      <p className="no-data-message">No attendance records found</p>
                    )}
                  </div>
                </>
              )}
            </div>
          </div>
        )}

        {/* Results Tab */}
        {activeTab === 'results' && (
          <div className="tab-panel">
            <div className="two-column-layout">
              <div className="left-column">
                <div className="students-list-container">
                  <h3>Students List</h3>
                  {students.length > 0 ? (
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>Name</th>
                          <th>Email</th>
                          <th>Roll Number</th>
                          <th>Department</th>
                          <th>Year</th>
                          <th>User ID</th>
                          <th>Action</th>
                        </tr>
                      </thead>
                      <tbody>
                        {students.map((student) => (
                          <tr
                            key={student.userId}
                            className={selectedStudent?.userId === student.userId ? 'selected' : ''}
                            onClick={() => handleStudentSelect(student)}
                          >
                            <td><strong>{student.username}</strong></td>
                            <td>{student.userEmail}</td>
                            <td>{student.enrollmentNumber || '-'}</td>
                            <td>{student.department || '-'}</td>
                            <td>{student.yearOfStudying ?? '-'}</td>
                            <td>{student.userId}</td>
                            <td>
                              <div className="table-actions">
                                <button className="icon-btn view" title="View">
                                  <FiEye />
                                </button>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  ) : (
                    <p className="no-data">No students found</p>
                  )}
                </div>
              </div>

              <div className="right-column">
                {selectedStudent ? (
                  <>
                    <div className="marks-form">
                      <h3>Enter Marks for {selectedStudent.username}</h3>

                      <div className="form-group">
                        <label htmlFor="semester">Semester:</label>
                        <select
                          id="semester"
                          value={markUpdate.semester}
                          onChange={(e) =>
                            setMarkUpdate((prev) => ({
                              ...prev,
                              semester: parseInt(e.target.value),
                            }))
                          }
                        >
                          {[1, 2, 3, 4, 5, 6, 7, 8].map((sem) => (
                            <option key={sem} value={sem}>
                              Semester {sem}
                            </option>
                          ))}
                        </select>
                      </div>

                      <div className="marks-grid">
                        {[1, 2, 3, 4, 5, 6].map((subject) => (
                          <div key={subject} className="form-group">
                            <label htmlFor={`subject${subject}`}>Subject {subject}</label>
                            <input
                              type="number"
                              id={`subject${subject}`}
                              min="0"
                              max="100"
                              value={markUpdate[`subject${subject}Mark`]}
                              onChange={(e) =>
                                setMarkUpdate((prev) => ({
                                  ...prev,
                                  [`subject${subject}Mark`]: e.target.value,
                                }))
                              }
                              placeholder="0"
                            />
                          </div>
                        ))}
                      </div>

                      <button className="btn btn-primary" onClick={handleSaveMarks}>
                        Save Marks
                      </button>
                    </div>

                    {Object.keys(studentMarks).length > 0 && (
                      <div className="marks-display">
                        <h3>Student Marks</h3>
                        <div className="marks-history">
                          {Array.isArray(studentMarks)
                            ? studentMarks.map((mark, index) => (
                                <div key={index} className="semester-marks">
                                  <h4>Semester {mark.semester}</h4>
                                  <div className="marks-row">
                                    {[1, 2, 3, 4, 5, 6].map((subject) => (
                                      <div key={subject} className="mark-item">
                                        <span>S{subject}</span>
                                        <p>{mark[`subject${subject}Mark`] || '-'}</p>
                                      </div>
                                    ))}
                                  </div>
                                  <p className="sgpa">SGPA: {mark.sgpa?.toFixed(2) || '-'}</p>
                                </div>
                              ))
                            : typeof studentMarks === 'object' && (
                                <div className="semester-marks">
                                  <h4>Semester {studentMarks.semester}</h4>
                                  <div className="marks-row">
                                    {[1, 2, 3, 4, 5, 6].map((subject) => (
                                      <div key={subject} className="mark-item">
                                        <span>S{subject}</span>
                                        <p>{studentMarks[`subject${subject}Mark`] || '-'}</p>
                                      </div>
                                    ))}
                                  </div>
                                  <p className="sgpa">
                                    SGPA: {studentMarks.sgpa?.toFixed(2) || '-'}
                                  </p>
                                </div>
                              )}
                        </div>
                      </div>
                    )}
                  </>
                ) : (
                  <div className="empty-state">
                    <p>Select a student to manage results</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Courses Tab */}
        {activeTab === 'courses' && (
          <div className="tab-panel">
            <div className="courses-management">
              <h3>Student Courses Management</h3>
              <p>Select a student to view and manage their courses</p>
              {selectedStudent ? (
                <div className="course-info">
                  <p>Managing courses for: <strong>{selectedStudent.username}</strong></p>
                  <div className="form-group" style={{ maxWidth: '220px', marginTop: '10px' }}>
                    <label htmlFor="courseSemester">Semester</label>
                    <select
                      id="courseSemester"
                      value={courseSemester}
                      onChange={(e) => setCourseSemester(parseInt(e.target.value))}
                    >
                      {[1, 2, 3, 4, 5, 6, 7, 8].map((sem) => (
                        <option key={sem} value={sem}>
                          Semester {sem}
                        </option>
                      ))}
                    </select>
                  </div>
                  {courseSelectionStatus && (
                    <p style={{ marginTop: '8px' }}>
                      Core: <strong>{courseSelectionStatus.coreCoursesSelected}/{courseSelectionStatus.maxCoreAllowed}</strong> | Core Elective: <strong>{courseSelectionStatus.coreElectiveCoursesSelected}/{courseSelectionStatus.maxCoreElectiveAllowed}</strong> | Open Elective: <strong>{courseSelectionStatus.openElectiveCoursesSelected}/{courseSelectionStatus.maxOpenElectiveAllowed}</strong>
                    </p>
                  )}

                  {studentSelectedCourses.length > 0 ? (
                    <div className="table-container" style={{ marginTop: '14px' }}>
                      <table className="history-table">
                        <thead>
                          <tr>
                            <th>Selected Course</th>
                            <th>Type</th>
                            <th>Replace With</th>
                            <th>Action</th>
                          </tr>
                        </thead>
                        <tbody>
                          {studentSelectedCourses.map((selectedCourse) => {
                            const selectedCourseIds = new Set(studentSelectedCourses.map((item) => item.courseId));
                            const replacementOptions = availableSemesterCourses.filter(
                              (course) =>
                                course.courseId !== selectedCourse.courseId &&
                                !selectedCourseIds.has(course.courseId)
                            );

                            return (
                              <tr key={selectedCourse.courseId}>
                                <td>{selectedCourse.courseName} ({selectedCourse.courseCode})</td>
                                <td>{selectedCourse.courseStatus}</td>
                                <td>
                                  <select
                                    value={replacementCourseIds[selectedCourse.courseId] || ''}
                                    onChange={(e) =>
                                      setReplacementCourseIds((prev) => ({
                                        ...prev,
                                        [selectedCourse.courseId]: e.target.value,
                                      }))
                                    }
                                  >
                                    <option value="">Select course</option>
                                    {replacementOptions.map((course) => (
                                      <option key={course.courseId} value={course.courseId}>
                                        {course.courseName} ({course.courseCode}) - {course.courseStatus}
                                      </option>
                                    ))}
                                  </select>
                                </td>
                                <td>
                                  <button
                                    className="btn-secondary"
                                    onClick={() => handleReplaceCourse(selectedCourse.courseId)}
                                  >
                                    Replace
                                  </button>
                                </td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    </div>
                  ) : (
                    <p className="empty-state" style={{ marginTop: '12px' }}>
                      No courses selected for Semester {courseSemester}
                    </p>
                  )}
                </div>
              ) : (
                <p className="empty-state">Select a student from the list to manage courses</p>
              )}
            </div>
          </div>
        )}

        {/* Add Student Tab */}
        {activeTab === 'addStudent' && (
          <div className="tab-panel">
            <div className="add-student-form">
              <h3>Add New Student</h3>

              <form>
                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="userId">User ID *</label>
                    <input
                      type="text"
                      id="userId"
                      value={newStudent.userId}
                      onChange={(e) =>
                        setNewStudent((prev) => ({ ...prev, userId: e.target.value }))
                      }
                      placeholder="Enter unique User ID (e.g., APVD1001)"
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="username">Full Name *</label>
                    <input
                      type="text"
                      id="username"
                      value={newStudent.username}
                      onChange={(e) =>
                        setNewStudent((prev) => ({ ...prev, username: e.target.value }))
                      }
                      placeholder="Enter student name"
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="email">Email *</label>
                    <input
                      type="email"
                      id="email"
                      value={newStudent.userEmail}
                      onChange={(e) =>
                        setNewStudent((prev) => ({ ...prev, userEmail: e.target.value }))
                      }
                      placeholder="Enter email"
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="password">Password *</label>
                    <input
                      type="password"
                      id="password"
                      value={newStudent.userPassword}
                      onChange={(e) =>
                        setNewStudent((prev) => ({ ...prev, userPassword: e.target.value }))
                      }
                      placeholder="Enter password"
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="mobile">Mobile *</label>
                    <input
                      type="tel"
                      id="mobile"
                      value={newStudent.mobile}
                      onChange={(e) =>
                        setNewStudent((prev) => ({ ...prev, mobile: e.target.value }))
                      }
                      placeholder="Enter mobile number"
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="enrollmentNumber">Enrollment Number</label>
                    <input
                      type="text"
                      id="enrollmentNumber"
                      value={newStudent.enrollmentNumber}
                      onChange={(e) =>
                        setNewStudent((prev) => ({
                          ...prev,
                          enrollmentNumber: e.target.value,
                        }))
                      }
                      placeholder="Enter enrollment number"
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="yearOfStudying">Year of Studying</label>
                    <select
                      id="yearOfStudying"
                      value={newStudent.yearOfStudying}
                      onChange={(e) =>
                        setNewStudent((prev) => ({
                          ...prev,
                          yearOfStudying: parseInt(e.target.value),
                        }))
                      }
                    >
                      {[1, 2, 3, 4].map((year) => (
                        <option key={year} value={year}>
                          Year {year}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="department">Department</label>
                  <input
                    type="text"
                    id="department"
                    disabled
                    value={facultyDepartment || user?.department || ''}
                    placeholder="Department"
                  />
                </div>

                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleAddStudent}
                >
                  Add Student
                </button>
              </form>
            </div>
          </div>
        )}

        {/* Students List Tab */}
        {activeTab === 'students' && (
          <div className="tab-panel">
            <div className="students-list-container">
              <h3><FiUsers style={{ marginRight: '8px' }} />Students List</h3>
              <div className="search-section">
                <input
                  type="text"
                  placeholder="Search students by name, email, or roll number..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="search-input"
                />
              </div>
              {students.length > 0 ? (
                <div className="table-wrapper">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Roll Number</th>
                        <th>Department</th>
                        <th>Year</th>
                        <th>Mobile</th>
                        <th>User ID</th>
                      </tr>
                    </thead>
                    <tbody>
                      {students
                        .filter((student) =>
                          student.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          student.userEmail.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          (student.enrollmentNumber && student.enrollmentNumber.toLowerCase().includes(searchQuery.toLowerCase()))
                        )
                        .map((student) => (
                          <tr key={student.userId}>
                            <td><strong>{student.username}</strong></td>
                            <td>{student.userEmail}</td>
                            <td>{student.enrollmentNumber || '-'}</td>
                            <td>{student.department || '-'}</td>
                            <td>{student.yearOfStudying ?? '-'}</td>
                            <td>{student.mobile || '-'}</td>
                            <td>{student.userId}</td>
                          </tr>
                        ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <p className="no-data-message">No students found</p>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default FacultyDashboard;
