import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useAuth } from '../services/AuthContext';
import { attendanceAPI, marksAPI, userAPI, departmentAPI, courseAPI } from '../services/api';
import { FiUsers, FiBookOpen, FiBarChart2, FiSettings, FiPlus, FiTrash2, FiEdit2, FiEye, FiDownload, FiX, FiUser, FiBriefcase, FiUpload } from 'react-icons/fi';
import { MdCheckCircle, MdCancel } from 'react-icons/md';
import '../styles/Dashboard.css';

const AdminDashboard = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('attendance');
  const [students, setStudents] = useState([]);
  const [faculties, setFaculties] = useState([]);
  const [selectedStudent, setSelectedStudent] = useState(null);
  const [studentMarks, setStudentMarks] = useState({});
  const [marksSubjects, setMarksSubjects] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [newStudent, setNewStudent] = useState({
    userId: '',
    username: '',
    userEmail: '',
    userPassword: '',
    mobile: '',
    enrollmentNumber: '',
    department: '',
    yearOfStudying: 1,
  });
  const [newFaculty, setNewFaculty] = useState({
    userId: '',
    username: '',
    userEmail: '',
    userPassword: '',
    mobile: '',
    department: '',
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
    department: '',
    year: '',
  });
  const [attendanceMarkingData, setAttendanceMarkingData] = useState({
    date: new Date().toISOString().split('T')[0],
    session: 'FN',
    remarks: 'Marked by Admin',
  });
  const [selectedStudentsForAttendance, setSelectedStudentsForAttendance] = useState(new Set());
  const [departments, setDepartments] = useState([]);
  const [newDepartment, setNewDepartment] = useState({
    departmentName: '',
    departmentCode: '',
    description: '',
  });
  const [studentAddMode, setStudentAddMode] = useState('manual');
  const [facultyAddMode, setFacultyAddMode] = useState('manual');
  const [courseAddMode, setCourseAddMode] = useState('manual');
  const [departmentAddMode, setDepartmentAddMode] = useState('manual');
  const [studentUploadFile, setStudentUploadFile] = useState(null);
  const [facultyUploadFile, setFacultyUploadFile] = useState(null);
  const [courseUploadFile, setCourseUploadFile] = useState(null);
  const [departmentUploadFile, setDepartmentUploadFile] = useState(null);
  const [courses, setCourses] = useState([]);
  const [newCourse, setNewCourse] = useState({
    courseCode: '',
    courseName: '',
    department: '',
    semester: 1,
    courseStatus: 'CORE',
    facultyUserId: '',
    description: '',
    credits: 3,
    capacity: 60,
  });
  const [editingDepartment, setEditingDepartment] = useState(null);
  const [attendanceTableData, setAttendanceTableData] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [usersStudentSearchQuery, setUsersStudentSearchQuery] = useState('');
  const [usersFacultySearchQuery, setUsersFacultySearchQuery] = useState('');
  const [attendanceSearchQuery, setAttendanceSearchQuery] = useState('');
  const [showMarkAttendance, setShowMarkAttendance] = useState(false);
  const [editingAttendanceId, setEditingAttendanceId] = useState(null);
  const [toast, setToast] = useState({ message: '', visible: false });
  const [facultyReassignTargets, setFacultyReassignTargets] = useState({});
  const initialLoadUserRef = useRef('');

  const getFacultyLabel = (facultyUserId) => {
    if (!facultyUserId) return '-';
    const faculty = faculties.find((item) => item.userId === facultyUserId);
    if (!faculty) return '-';
    return faculty.department ? `${faculty.username} (${faculty.department})` : faculty.username;
  };

  const showToast = (message) => {
    setToast({ message, visible: true });
    const timer = setTimeout(() => {
      setToast({ message: '', visible: false });
    }, 3000);
    return () => clearTimeout(timer);
  };

  const fetchAttendanceData = useCallback(async (studentList = []) => {
    try {
      const allStudents = studentList || [];
      
      if (allStudents.length > 0) {
        const userIds = allStudents.map(s => s.userId);
        const attendanceRes = await attendanceAPI.getAttendanceByStudents(userIds);
        setAttendanceTableData(attendanceRes.data || []);
      } else {
        setAttendanceTableData([]);
      }
    } catch (error) {
      console.error('Failed to fetch attendance data:', error);
    }
  }, []);

  const fetchStudents = useCallback(async () => {
    setLoading(true);
    try {
      const response = await userAPI.getUsersByRole('STUDENT');
      const allStudents = response.data || [];
      setStudents(allStudents);
      await fetchAttendanceData(allStudents);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch students');
    } finally {
      setLoading(false);
    }
  }, [fetchAttendanceData]);

  const fetchFaculties = useCallback(async () => {
    try {
      const response = await userAPI.getUsersByRole('FACULTY');
      setFaculties(response.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch faculties');
    }
  }, []);

  const fetchDepartments = useCallback(async () => {
    try {
      const response = await departmentAPI.getAllDepartments();
      setDepartments(response.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch departments');
    }
  }, []);

  const fetchCourses = useCallback(async () => {
    try {
      const response = await courseAPI.getAllCourses();
      setCourses(response.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch courses');
    }
  }, []);

  useEffect(() => {
    if (!user?.userId) return;
    if (initialLoadUserRef.current === user.userId) return;

    initialLoadUserRef.current = user.userId;
    fetchStudents();
    fetchFaculties();
    fetchDepartments();
    fetchCourses();
  }, [user, fetchStudents, fetchFaculties, fetchDepartments, fetchCourses]);

  useEffect(() => {
    if (activeTab === 'courses') {
      fetchCourses();
    }
  }, [activeTab, fetchCourses]);

  const getFilteredStudents = () => {
    let filtered = students.filter((student) => {
      if (attendanceFilters.department && student.department !== attendanceFilters.department) {
        return false;
      }
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
      fetchAttendanceData(students);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to mark attendance');
    } finally {
      setLoading(false);
    }
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
      fetchAttendanceData(students);
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
      fetchAttendanceData(students);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to mark attendance');
    } finally {
      setLoading(false);
    }
  };

  const handleStudentSelect = async (student) => {
    setSelectedStudent(student);
    setError('');

    try {
      const marksRes = await marksAPI.getStudentMarks(student.userId);
      setStudentMarks(marksRes.data || {});
    } catch {
      setError('Failed to fetch student data');
    }
  };

  useEffect(() => {
    const loadMarksSubjects = async () => {
      if (activeTab !== 'results' || !selectedStudent?.userId || !markUpdate?.semester) {
        setMarksSubjects([]);
        return;
      }

      try {
        const selectedRes = await courseAPI.getStudentCourses(selectedStudent.userId, markUpdate.semester);
        const selected = selectedRes.data || [];
        setMarksSubjects(selected.slice(0, 6).map((course) => course.courseName));
      } catch (err) {
        console.error('Failed to load selected subjects for marks:', err);
        setMarksSubjects([]);
      }
    };

    loadMarksSubjects();
  }, [activeTab, selectedStudent, markUpdate.semester]);

  const isGenericSubjectName = (name, subjectNumber) => {
    if (!name || !String(name).trim()) return true;
    const normalized = String(name).trim().toLowerCase();
    return normalized === `s${subjectNumber}` || normalized === `subject ${subjectNumber}`;
  };

  const resolveSubjectName = (markData, subjectNumber) => {
    const selectedCourseName = marksSubjects[subjectNumber - 1];
    if (selectedCourseName) {
      return selectedCourseName;
    }

    const savedName = markData?.[`subject${subjectNumber}Name`];
    if (!isGenericSubjectName(savedName, subjectNumber)) {
      return savedName;
    }

    return `Subject ${subjectNumber}`;
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
        subject1Name: marksSubjects[0] || 'Subject 1',
        subject2Name: marksSubjects[1] || 'Subject 2',
        subject3Name: marksSubjects[2] || 'Subject 3',
        subject4Name: marksSubjects[3] || 'Subject 4',
        subject5Name: marksSubjects[4] || 'Subject 5',
        subject6Name: marksSubjects[5] || 'Subject 6',
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
        !newStudent.mobile ||
        !newStudent.department
      ) {
        setError('Please fill in all required fields');
        return;
      }

      await userAPI.addStudent(newStudent);

      setNewStudent({
        userId: '',
        username: '',
        userEmail: '',
        userPassword: '',
        mobile: '',
        enrollmentNumber: '',
        department: '',
        yearOfStudying: 1,
      });

      showToast('Student added successfully!');
      fetchStudents();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add student');
    }
  };

  const handleAddCourse = async () => {
    try {
      if (
        !newCourse.courseCode ||
        !newCourse.courseName ||
        !newCourse.department ||
        !newCourse.semester ||
        !newCourse.courseStatus ||
        !newCourse.facultyUserId
      ) {
        setError('Please fill in all required course fields');
        return;
      }

      await courseAPI.addCourse({
        ...newCourse,
        semester: parseInt(newCourse.semester),
        credits: parseInt(newCourse.credits),
        capacity: parseInt(newCourse.capacity),
      });

      showToast('Course added successfully!');
      setError('');
      setNewCourse({
        courseCode: '',
        courseName: '',
        department: '',
        semester: 1,
        courseStatus: 'CORE',
        facultyUserId: '',
        description: '',
        credits: 3,
        capacity: 60,
      });
      fetchCourses();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add course');
    }
  };

  const handleAddFaculty = async () => {
    try {
      if (
        !newFaculty.userId ||
        !newFaculty.username ||
        !newFaculty.userEmail ||
        !newFaculty.userPassword ||
        !newFaculty.mobile ||
        !newFaculty.department
      ) {
        setError('Please fill in all required fields');
        return;
      }

      await userAPI.addFaculty(newFaculty);

      setNewFaculty({
        userId: '',
        username: '',
        userEmail: '',
        userPassword: '',
        mobile: '',
        department: '',
      });

      showToast('Faculty added successfully!');
      fetchFaculties();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add faculty');
    }
  };

  const handleAddDepartment = async () => {
    try {
      if (!newDepartment.departmentName) {
        setError('Department name is required');
        return;
      }

      await departmentAPI.addDepartment(newDepartment);
      
      setNewDepartment({
        departmentName: '',
        departmentCode: '',
        description: '',
      });

      showToast('Department added successfully!');
      fetchDepartments();
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add department');
    }
  };

  const applyUploadResult = (entityLabel, result, clearFile) => {
    const totalRows = result?.totalRows || 0;
    const successCount = result?.successCount || 0;
    const failedCount = result?.failedCount || 0;
    const errors = result?.errors || [];

    showToast(`${entityLabel} upload completed: ${successCount}/${totalRows} successful`);

    if (failedCount > 0) {
      setError(
        `${entityLabel} upload has ${failedCount} failed row(s). ${errors.slice(0, 3).join(' | ')}`
      );
    } else {
      setError('');
    }

    clearFile(null);
  };

  const handleUploadStudents = async () => {
    if (!studentUploadFile) {
      setError('Please choose a student Excel file first');
      return;
    }

    try {
      setLoading(true);
      const response = await userAPI.uploadStudents(studentUploadFile);
      applyUploadResult('Student', response.data, setStudentUploadFile);
      fetchStudents();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload students');
    } finally {
      setLoading(false);
    }
  };

  const handleUploadFaculties = async () => {
    if (!facultyUploadFile) {
      setError('Please choose a faculty Excel file first');
      return;
    }

    try {
      setLoading(true);
      const response = await userAPI.uploadFaculties(facultyUploadFile);
      applyUploadResult('Faculty', response.data, setFacultyUploadFile);
      fetchFaculties();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload faculties');
    } finally {
      setLoading(false);
    }
  };

  const handleUploadCourses = async () => {
    if (!courseUploadFile) {
      setError('Please choose a course Excel file first');
      return;
    }

    try {
      setLoading(true);
      const response = await courseAPI.uploadCourses(courseUploadFile);
      applyUploadResult('Course', response.data, setCourseUploadFile);
      fetchCourses();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload courses');
    } finally {
      setLoading(false);
    }
  };

  const handleUploadDepartments = async () => {
    if (!departmentUploadFile) {
      setError('Please choose a department Excel file first');
      return;
    }

    try {
      setLoading(true);
      const response = await departmentAPI.uploadDepartments(departmentUploadFile);
      applyUploadResult('Department', response.data, setDepartmentUploadFile);
      fetchDepartments();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload departments');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateDepartment = async () => {
    try {
      if (!editingDepartment.departmentName) {
        setError('Department name is required');
        return;
      }

      await departmentAPI.updateDepartment(
        editingDepartment.departmentId,
        editingDepartment
      );

      setEditingDepartment(null);
      showToast('Department updated successfully!');
      fetchDepartments();
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update department');
    }
  };

  const handleDeleteDepartment = async (departmentId) => {
    if (!window.confirm('Are you sure you want to delete this department?')) {
      return;
    }

    try {
      await departmentAPI.deleteDepartment(departmentId);
      showToast('Department deleted successfully!');
      fetchDepartments();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete department');
    }
  };

  const handleDeleteStudent = async (studentUserId) => {
    if (!window.confirm('Are you sure you want to delete this student?')) {
      return;
    }

    try {
      await userAPI.deleteUser(studentUserId);
      showToast('Student deleted successfully!');
      if (selectedStudent?.userId === studentUserId) {
        setSelectedStudent(null);
      }
      fetchStudents();
    } catch (err) {
      setError(
        err.response?.data?.message ||
        (typeof err.response?.data === 'string' ? err.response.data : null) ||
        'Failed to delete student'
      );
    }
  };

  const handleDeleteFaculty = async (facultyUserId) => {
    if (!window.confirm('Are you sure you want to delete this faculty?')) {
      return;
    }

    try {
      await userAPI.deleteUser(facultyUserId);
      showToast('Faculty deleted successfully!');
      fetchFaculties();
      fetchCourses();
    } catch (err) {
      setError(
        err.response?.data?.message ||
        (typeof err.response?.data === 'string' ? err.response.data : null) ||
        'Failed to delete faculty. Reassign courses before deleting.'
      );
    }
  };

  const handleDeleteCourse = async (courseId) => {
    if (!window.confirm('Are you sure you want to delete this course?')) {
      return;
    }

    try {
      await courseAPI.deleteCourse(courseId);
      showToast('Course deleted successfully!');
      fetchCourses();
    } catch (err) {
      setError(
        err.response?.data?.message ||
        (typeof err.response?.data === 'string' ? err.response.data : null) ||
        'Failed to delete course'
      );
    }
  };

  const handleReassignFacultyCourses = async (fromFacultyUserId) => {
    const toFacultyUserId = facultyReassignTargets[fromFacultyUserId];

    if (!toFacultyUserId) {
      setError('Please select target faculty for reallocation');
      return;
    }

    if (toFacultyUserId === fromFacultyUserId) {
      setError('Please select a different faculty for reallocation');
      return;
    }

    try {
      const response = await courseAPI.reassignFacultyCourses(fromFacultyUserId, toFacultyUserId);
      const reassignedCount = response.data?.reassignedCount ?? 0;
      showToast(`Reassigned ${reassignedCount} course(s) successfully!`);
      setFacultyReassignTargets((prev) => ({ ...prev, [fromFacultyUserId]: '' }));
      fetchCourses();
    } catch (err) {
      setError(
        err.response?.data?.message ||
        (typeof err.response?.data === 'string' ? err.response.data : null) ||
        'Failed to reassign faculty courses'
      );
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
        <h1>Admin Dashboard</h1>
        <p>Welcome, Admin {user?.username}!</p>
      </div>

      <div className="dashboard-tabs">
        <button
          className={`tab-button ${activeTab === 'studentsList' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('studentsList');
            setError('');
          }}
        >
          <FiUsers style={{ marginRight: '4px' }} />
          Students List
        </button>
        <button
          className={`tab-button ${activeTab === 'facultyList' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('facultyList');
            setError('');
          }}
        >
          <FiUser style={{ marginRight: '4px' }} />
          Faculty List
        </button>
        <button
          className={`tab-button ${activeTab === 'attendance' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('attendance');
            setError('');
          }}
        >
          <FiBarChart2 style={{ marginRight: '4px' }} />
          Attendance
        </button>
        <button
          className={`tab-button ${activeTab === 'results' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('results');
            setError('');
          }}
        >
          <FiBarChart2 style={{ marginRight: '4px' }} />
          Results
        </button>
        <button
          className={`tab-button ${activeTab === 'courses' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('courses');
            setError('');
          }}
        >
          <FiBookOpen style={{ marginRight: '4px' }} />
          Courses
        </button>
        <button
          className={`tab-button ${activeTab === 'addStudent' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('addStudent');
            setError('');
          }}
        >
          <FiPlus style={{ marginRight: '4px' }} />
          Add Student
        </button>
        <button
          className={`tab-button ${activeTab === 'addFaculty' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('addFaculty');
            setError('');
          }}
        >
          <FiUser style={{ marginRight: '4px' }} />
          Add Faculty
        </button>
        <button
          className={`tab-button ${activeTab === 'departments' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('departments');
            setError('');
          }}
        >
          <FiBriefcase style={{ marginRight: '4px' }} />
          Departments
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
                      placeholder="e.g., Marked by Admin"
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
                  <div className="filters-grid list-filters-inline">
                    <div className="filter-group">
                      <label>Department:</label>
                      <select
                        value={attendanceFilters.department}
                        onChange={(e) => setAttendanceFilters({ ...attendanceFilters, department: e.target.value })}
                      >
                        <option value="">All Departments</option>
                        {departments.map((dept) => (
                          <option key={dept.departmentId} value={dept.departmentName}>
                            {dept.departmentName}
                          </option>
                        ))}
                      </select>
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
              <div className="students-list-container">
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
                          <td>{record.department}</td>
                          <td>{record.yearOfStudying ?? '-'}</td>
                          <td>{new Date(record.attendanceDate).toLocaleDateString()}</td>
                          <td>
                            {record.fn ? (
                              <span className={`status-badge ${record.fn.status.toLowerCase()}`}>
                                {record.fn.status === 'PRESENT' ? (
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
                            ) : (
                              '-'
                            )}
                          </td>
                          <td>
                            {record.an ? (
                              <span className={`status-badge ${record.an.status.toLowerCase()}`}>
                                {record.an.status === 'PRESENT' ? (
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
                            ) : (
                              '-'
                            )}
                          </td>
                          <td>{record.remarks.length > 0 ? [...new Set(record.remarks)].join(', ') : '-'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                ) : (
                  <p className="no-data-message">No attendance records found</p>
                )}
              </div>
            )}
          </div>
        </div>
        )}

        {/* Results Tab */}
        {activeTab === 'results' && (
          <div className="tab-panel">
            <div className="students-list-container">
              <h3><FiBarChart2 style={{ marginRight: '8px' }} /> Students for Marks</h3>
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
                        <td>{student.department}</td>
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
                <p className="no-data-message">No students found</p>
              )}
            </div>

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
                            <label htmlFor={`subject${subject}`}>{marksSubjects[subject - 1] || `Subject ${subject}`}</label>
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

                      <button className="btn btn-primary btn-with-icon" onClick={handleSaveMarks}>
                        <FiDownload /> Save Marks
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
                                        <span>{resolveSubjectName(mark, subject)}</span>
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
                                        <span>{resolveSubjectName(studentMarks, subject)}</span>
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
        )}

        {/* Courses Tab */}
        {activeTab === 'courses' && (
          <div className="tab-panel">
            <div className="courses-management">
              <h3>Course Management</h3>
              <p>Only admin can add courses. Courses are semester and department specific.</p>

              <div className="entry-mode-toggle">
                <button
                  className={`mode-button ${courseAddMode === 'manual' ? 'active' : ''}`}
                  type="button"
                  onClick={() => setCourseAddMode('manual')}
                >
                  Manual Add
                </button>
                <button
                  className={`mode-button ${courseAddMode === 'upload' ? 'active' : ''}`}
                  type="button"
                  onClick={() => setCourseAddMode('upload')}
                >
                  Upload Excel
                </button>
              </div>

              {courseAddMode === 'manual' ? (
              <div className="add-student-form" style={{ marginTop: '12px' }}>
                <div className="form-row">
                  <div className="form-group">
                    <label>Course Code *</label>
                    <input
                      type="text"
                      value={newCourse.courseCode}
                      onChange={(e) => setNewCourse((prev) => ({ ...prev, courseCode: e.target.value }))}
                      placeholder="e.g., CSE301"
                    />
                  </div>
                  <div className="form-group">
                    <label>Course Name *</label>
                    <input
                      type="text"
                      value={newCourse.courseName}
                      onChange={(e) => setNewCourse((prev) => ({ ...prev, courseName: e.target.value }))}
                      placeholder="Course name"
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Department *</label>
                    <input
                      type="text"
                      value={newCourse.department}
                      onChange={(e) => setNewCourse((prev) => ({ ...prev, department: e.target.value }))}
                      placeholder="e.g., CSE"
                    />
                  </div>
                  <div className="form-group">
                    <label>Semester *</label>
                    <select
                      value={newCourse.semester}
                      onChange={(e) => setNewCourse((prev) => ({ ...prev, semester: e.target.value }))}
                    >
                      {[1, 2, 3, 4, 5, 6, 7, 8].map((sem) => (
                        <option key={sem} value={sem}>Semester {sem}</option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Course Type *</label>
                    <select
                      value={newCourse.courseStatus}
                      onChange={(e) => setNewCourse((prev) => ({ ...prev, courseStatus: e.target.value }))}
                    >
                      <option value="CORE">CORE</option>
                      <option value="CORE_ELECTIVE">CORE_ELECTIVE</option>
                      <option value="OPEN_ELECTIVE">OPEN_ELECTIVE</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Faculty *</label>
                    <select
                      value={newCourse.facultyUserId}
                      onChange={(e) => setNewCourse((prev) => ({ ...prev, facultyUserId: e.target.value }))}
                    >
                      <option value="">Select faculty</option>
                      {faculties.map((faculty) => (
                        <option key={faculty.userId} value={faculty.userId}>
                          {faculty.department ? `${faculty.username} (${faculty.department})` : faculty.username}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Credits</label>
                    <input
                      type="number"
                      min="1"
                      value={newCourse.credits}
                      onChange={(e) => setNewCourse((prev) => ({ ...prev, credits: e.target.value }))}
                    />
                  </div>
                  <div className="form-group">
                    <label>Capacity</label>
                    <input
                      type="number"
                      min="1"
                      value={newCourse.capacity}
                      onChange={(e) => setNewCourse((prev) => ({ ...prev, capacity: e.target.value }))}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Description</label>
                  <textarea
                    value={newCourse.description}
                    onChange={(e) => setNewCourse((prev) => ({ ...prev, description: e.target.value }))}
                    rows="2"
                    placeholder="Optional"
                  />
                </div>

                <div className="form-actions">
                  <button type="button" className="btn-primary" onClick={handleAddCourse}>
                    <FiPlus /> Add Course
                  </button>
                </div>
              </div>
              ) : (
              <div className="add-student-form" style={{ marginTop: '12px' }}>
                <h4 style={{ marginTop: 0 }}>Upload Courses from Excel</h4>
                <p className="upload-helper-text">
                  Column order: courseCode, courseName, department, semester, courseStatus, facultyUserId, description, credits, capacity
                </p>
                <input
                  type="file"
                  accept=".xlsx,.xls"
                  onChange={(e) => setCourseUploadFile(e.target.files?.[0] || null)}
                />
                <div className="form-actions" style={{ marginTop: '12px' }}>
                  <button type="button" className="btn-primary" onClick={handleUploadCourses} disabled={loading}>
                    <FiUpload /> Upload Courses
                  </button>
                </div>
              </div>
              )}

              <div className="table-container" style={{ marginTop: '16px' }}>
                {courses.length > 0 ? (
                  <table className="history-table">
                    <thead>
                      <tr>
                        <th>Code</th>
                        <th>Name</th>
                        <th>Department</th>
                        <th>Semester</th>
                        <th>Type</th>
                        <th>Faculty</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {courses.map((course) => (
                        <tr key={course.courseId}>
                          <td>{course.courseCode}</td>
                          <td>{course.courseName}</td>
                          <td>{course.department}</td>
                          <td>{course.semester}</td>
                          <td>{course.courseStatus}</td>
                          <td>{getFacultyLabel(course.facultyUserId)}</td>
                          <td>
                            <button
                              className="btn-icon btn-delete"
                              onClick={() => handleDeleteCourse(course.courseId)}
                              title="Delete Course"
                            >
                              <FiTrash2 />
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                ) : (
                  <p className="empty-state">No courses available</p>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Add Student Tab */}
        {activeTab === 'addStudent' && (
          <div className="tab-panel">
            <div className="add-student-form">
              <h3><FiUsers style={{ marginRight: '8px' }} /> Add New Student</h3>

              <div className="entry-mode-toggle">
                <button
                  className={`mode-button ${studentAddMode === 'manual' ? 'active' : ''}`}
                  type="button"
                  onClick={() => setStudentAddMode('manual')}
                >
                  Manual Add
                </button>
                <button
                  className={`mode-button ${studentAddMode === 'upload' ? 'active' : ''}`}
                  type="button"
                  onClick={() => setStudentAddMode('upload')}
                >
                  Upload Excel
                </button>
              </div>

              {studentAddMode === 'manual' ? (
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
                </div>

                <div className="form-row">
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
                  <label htmlFor="department">Department *</label>
                  <select
                    id="department"
                    value={newStudent.department}
                    onChange={(e) =>
                      setNewStudent((prev) => ({ ...prev, department: e.target.value }))
                    }
                    required
                  >
                    <option value="">-- Select Department --</option>
                    {departments.map((dept) => (
                      <option key={dept.departmentId} value={dept.departmentName}>
                        {dept.departmentName} {dept.departmentCode ? `(${dept.departmentCode})` : ''}
                      </option>
                    ))}
                  </select>
                </div>

                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleAddStudent}
                >
                  <FiPlus /> Add Student
                </button>
              </form>
              ) : (
                <div>
                  <p className="upload-helper-text">
                    Column order: userId, username, userEmail, userPassword, mobile, enrollmentNumber, department, yearOfStudying
                  </p>
                  <input
                    type="file"
                    accept=".xlsx,.xls"
                    onChange={(e) => setStudentUploadFile(e.target.files?.[0] || null)}
                  />
                  <div className="form-actions" style={{ marginTop: '12px' }}>
                    <button type="button" className="btn btn-primary" onClick={handleUploadStudents} disabled={loading}>
                      <FiUpload /> Upload Students
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Add Faculty Tab */}
        {activeTab === 'addFaculty' && (
          <div className="tab-panel">
            <div className="add-faculty-form">
              <h3>Add New Faculty</h3>

              <div className="entry-mode-toggle">
                <button
                  className={`mode-button ${facultyAddMode === 'manual' ? 'active' : ''}`}
                  type="button"
                  onClick={() => setFacultyAddMode('manual')}
                >
                  Manual Add
                </button>
                <button
                  className={`mode-button ${facultyAddMode === 'upload' ? 'active' : ''}`}
                  type="button"
                  onClick={() => setFacultyAddMode('upload')}
                >
                  Upload Excel
                </button>
              </div>

              {facultyAddMode === 'manual' ? (
              <form>
                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="fac-userId">User ID *</label>
                    <input
                      type="text"
                      id="fac-userId"
                      value={newFaculty.userId}
                      onChange={(e) =>
                        setNewFaculty((prev) => ({ ...prev, userId: e.target.value }))
                      }
                      placeholder="Enter unique User ID (e.g., APVD1001)"
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="fac-username">Full Name *</label>
                    <input
                      type="text"
                      id="fac-username"
                      value={newFaculty.username}
                      onChange={(e) =>
                        setNewFaculty((prev) => ({ ...prev, username: e.target.value }))
                      }
                      placeholder="Enter faculty name"
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="fac-email">Email *</label>
                    <input
                      type="email"
                      id="fac-email"
                      value={newFaculty.userEmail}
                      onChange={(e) =>
                        setNewFaculty((prev) => ({ ...prev, userEmail: e.target.value }))
                      }
                      placeholder="Enter email"
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="fac-password">Password *</label>
                    <input
                      type="password"
                      id="fac-password"
                      value={newFaculty.userPassword}
                      onChange={(e) =>
                        setNewFaculty((prev) => ({ ...prev, userPassword: e.target.value }))
                      }
                      placeholder="Enter password"
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="fac-mobile">Mobile *</label>
                    <input
                      type="tel"
                      id="fac-mobile"
                      value={newFaculty.mobile}
                      onChange={(e) =>
                        setNewFaculty((prev) => ({ ...prev, mobile: e.target.value }))
                      }
                      placeholder="Enter mobile number"
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="fac-department">Department *</label>
                  <select
                    id="fac-department"
                    value={newFaculty.department}
                    onChange={(e) =>
                      setNewFaculty((prev) => ({ ...prev, department: e.target.value }))
                    }
                    required
                  >
                    <option value="">-- Select Department --</option>
                    {departments.map((dept) => (
                      <option key={dept.departmentId} value={dept.departmentName}>
                        {dept.departmentName} {dept.departmentCode ? `(${dept.departmentCode})` : ''}
                      </option>
                    ))}
                  </select>
                </div>

                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleAddFaculty}
                >
                  Add Faculty
                </button>
              </form>
              ) : (
                <div>
                  <p className="upload-helper-text">
                    Column order: userId, username, userEmail, userPassword, mobile, department
                  </p>
                  <input
                    type="file"
                    accept=".xlsx,.xls"
                    onChange={(e) => setFacultyUploadFile(e.target.files?.[0] || null)}
                  />
                  <div className="form-actions" style={{ marginTop: '12px' }}>
                    <button type="button" className="btn btn-primary" onClick={handleUploadFaculties} disabled={loading}>
                      <FiUpload /> Upload Faculties
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Departments Tab */}
        {activeTab === 'departments' && (
          <div className="tab-panel">
            <div className="departments-container">
              <div className="department-form-section">
                <h3>{departmentAddMode === 'upload' ? 'Upload Departments' : (editingDepartment ? 'Edit Department' : 'Add New Department')}</h3>

                <div className="entry-mode-toggle">
                  <button
                    className={`mode-button ${departmentAddMode === 'manual' ? 'active' : ''}`}
                    type="button"
                    onClick={() => setDepartmentAddMode('manual')}
                  >
                    Manual Add
                  </button>
                  <button
                    className={`mode-button ${departmentAddMode === 'upload' ? 'active' : ''}`}
                    type="button"
                    onClick={() => {
                      setDepartmentAddMode('upload');
                      setEditingDepartment(null);
                    }}
                  >
                    Upload Excel
                  </button>
                </div>

                {departmentAddMode === 'manual' ? (
                <form>
                  <div className="form-group">
                    <label htmlFor="dept-name">Department Name *</label>
                    <input
                      type="text"
                      id="dept-name"
                      value={editingDepartment ? editingDepartment.departmentName : newDepartment.departmentName}
                      onChange={(e) => {
                        if (editingDepartment) {
                          setEditingDepartment((prev) => ({ ...prev, departmentName: e.target.value }));
                        } else {
                          setNewDepartment((prev) => ({ ...prev, departmentName: e.target.value }));
                        }
                      }}
                      placeholder="e.g., Computer Science, Electronics"
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="dept-code">Department Code</label>
                    <input
                      type="text"
                      id="dept-code"
                      value={editingDepartment ? editingDepartment.departmentCode : newDepartment.departmentCode}
                      onChange={(e) => {
                        if (editingDepartment) {
                          setEditingDepartment((prev) => ({ ...prev, departmentCode: e.target.value }));
                        } else {
                          setNewDepartment((prev) => ({ ...prev, departmentCode: e.target.value }));
                        }
                      }}
                      placeholder="e.g., CS, EC, EE"
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="dept-description">Description</label>
                    <textarea
                      id="dept-description"
                      rows="3"
                      value={editingDepartment ? editingDepartment.description : newDepartment.description}
                      onChange={(e) => {
                        if (editingDepartment) {
                          setEditingDepartment((prev) => ({ ...prev, description: e.target.value }));
                        } else {
                          setNewDepartment((prev) => ({ ...prev, description: e.target.value }));
                        }
                      }}
                      placeholder="Brief description of the department"
                    ></textarea>
                  </div>

                  <div className="button-group">
                    {editingDepartment ? (
                      <>
                        <button
                          type="button"
                          className="btn btn-primary"
                          onClick={handleUpdateDepartment}
                        >
                          Update Department
                        </button>
                        <button
                          type="button"
                          className="btn btn-secondary"
                          onClick={() => setEditingDepartment(null)}
                        >
                          Cancel
                        </button>
                      </>
                    ) : (
                      <button
                        type="button"
                        className="btn btn-primary"
                        onClick={handleAddDepartment}
                      >
                        Add Department
                      </button>
                    )}
                  </div>
                </form>
                ) : (
                  <div>
                    <p className="upload-helper-text">
                      Column order: departmentName, departmentCode, description
                    </p>
                    <input
                      type="file"
                      accept=".xlsx,.xls"
                      onChange={(e) => setDepartmentUploadFile(e.target.files?.[0] || null)}
                    />
                    <div className="form-actions" style={{ marginTop: '12px' }}>
                      <button type="button" className="btn btn-primary" onClick={handleUploadDepartments} disabled={loading}>
                        <FiUpload /> Upload Departments
                      </button>
                    </div>
                  </div>
                )}
              </div>

              <div className="department-list-section">
                <h3>Existing Departments</h3>
                {departments.length > 0 ? (
                  <div className="departments-table">
                    <table>
                      <thead>
                        <tr>
                          <th>Name</th>
                          <th>Code</th>
                          <th>Description</th>
                          <th>Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {departments.map((dept) => (
                          <tr key={dept.departmentId}>
                            <td>{dept.departmentName}</td>
                            <td>{dept.departmentCode || 'N/A'}</td>
                            <td>{dept.description || 'N/A'}</td>
                            <td>
                              <button
                                className="btn-icon btn-edit"
                                onClick={() => setEditingDepartment(dept)}
                                title="Edit"
                              >
                                <FiEdit2 />
                              </button>
                              <button
                                className="btn-icon btn-delete"
                                onClick={() => handleDeleteDepartment(dept.departmentId)}
                                title="Delete"
                              >
                                <FiTrash2 />
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <p className="no-data">No departments found. Add one above!</p>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Students List Tab */}
        {activeTab === 'studentsList' && (
          <div className="tab-panel">
            <div className="students-list-container">
              <h3><FiUsers style={{ marginRight: '8px' }} />Students List</h3>
              <div className="search-section">
                <input
                  type="text"
                  placeholder="Search students by name, email, roll number, or department..."
                  value={usersStudentSearchQuery}
                  onChange={(e) => setUsersStudentSearchQuery(e.target.value)}
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
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {students
                        .filter((student) => {
                          const query = usersStudentSearchQuery.toLowerCase();
                          if (!query) return true;
                          return (
                            (student.username || '').toLowerCase().includes(query) ||
                            (student.userEmail || '').toLowerCase().includes(query) ||
                            (student.enrollmentNumber || '').toLowerCase().includes(query) ||
                            (student.department || '').toLowerCase().includes(query) ||
                            (student.mobile || '').toLowerCase().includes(query) ||
                            (student.userId || '').toLowerCase().includes(query)
                          );
                        })
                        .map((student) => (
                          <tr key={student.userId}>
                            <td><strong>{student.username}</strong></td>
                            <td>{student.userEmail}</td>
                            <td>{student.enrollmentNumber || '-'}</td>
                            <td>{student.department || '-'}</td>
                            <td>{student.yearOfStudying ?? '-'}</td>
                            <td>{student.mobile || '-'}</td>
                            <td>{student.userId}</td>
                            <td>
                              <button
                                className="btn-icon btn-delete"
                                onClick={() => handleDeleteStudent(student.userId)}
                                title="Delete Student"
                              >
                                <FiTrash2 />
                              </button>
                            </td>
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

        {/* Faculty List Tab */}
        {activeTab === 'facultyList' && (
          <div className="tab-panel">
            <div className="students-list-container">
              <h3><FiUser style={{ marginRight: '8px' }} />Faculty List</h3>
              <div className="search-section">
                <input
                  type="text"
                  placeholder="Search faculty by name, email, or department..."
                  value={usersFacultySearchQuery}
                  onChange={(e) => setUsersFacultySearchQuery(e.target.value)}
                  className="search-input"
                />
              </div>
              {faculties.length > 0 ? (
                <div className="table-wrapper">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Department</th>
                        <th>Mobile</th>
                        <th>User ID</th>
                        <th>Reallocate Courses</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {faculties
                        .filter((faculty) => {
                          const query = usersFacultySearchQuery.toLowerCase();
                          if (!query) return true;
                          return (
                            (faculty.username || '').toLowerCase().includes(query) ||
                            (faculty.userEmail || '').toLowerCase().includes(query) ||
                            (faculty.department || '').toLowerCase().includes(query) ||
                            (faculty.mobile || '').toLowerCase().includes(query) ||
                            (faculty.userId || '').toLowerCase().includes(query)
                          );
                        })
                        .map((faculty) => (
                          <tr key={faculty.userId}>
                            <td><strong>{faculty.username}</strong></td>
                            <td>{faculty.userEmail}</td>
                            <td>{faculty.department || '-'}</td>
                            <td>{faculty.mobile || '-'}</td>
                            <td>{faculty.userId}</td>
                            <td>
                              <div className="faculty-reassign-row">
                                <select
                                  value={facultyReassignTargets[faculty.userId] || ''}
                                  onChange={(e) =>
                                    setFacultyReassignTargets((prev) => ({
                                      ...prev,
                                      [faculty.userId]: e.target.value,
                                    }))
                                  }
                                >
                                  <option value="">Select Faculty</option>
                                  {faculties
                                    .filter((item) => item.userId !== faculty.userId)
                                    .map((item) => (
                                      <option key={item.userId} value={item.userId}>
                                        {item.username} ({item.userId})
                                      </option>
                                    ))}
                                </select>
                                <button
                                  className="btn btn-secondary btn-sm"
                                  onClick={() => handleReassignFacultyCourses(faculty.userId)}
                                  type="button"
                                >
                                  Reallocate
                                </button>
                              </div>
                            </td>
                            <td>
                              <button
                                className="btn-icon btn-delete"
                                onClick={() => handleDeleteFaculty(faculty.userId)}
                                title="Delete Faculty"
                              >
                                <FiTrash2 />
                              </button>
                            </td>
                          </tr>
                        ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <p className="no-data-message">No faculty found</p>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminDashboard;
