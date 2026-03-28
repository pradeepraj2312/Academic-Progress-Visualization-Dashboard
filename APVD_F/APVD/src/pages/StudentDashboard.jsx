import React, { useState, useEffect } from 'react';
import { useAuth } from '../services/AuthContext';
import { attendanceAPI, marksAPI, courseAPI, gradesAPI } from '../services/api';
import { FiBarChart2, FiBookOpen, FiEye, FiDownload } from 'react-icons/fi';import { Line, Bar, Doughnut } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import '../styles/Dashboard.css';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

const StudentDashboard = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('attendance');
  const [attendance, setAttendance] = useState([]);
  const [attendancePercentage, setAttendancePercentage] = useState(0);
  const [marks, setMarks] = useState([]);
  const [allSemesterMarks, setAllSemesterMarks] = useState([]);
  const [cgpa, setCGPA] = useState(0);
  const [courses, setCourses] = useState([]);
  const [selectedCourses, setSelectedCourses] = useState([]);
  const [selectionStatus, setSelectionStatus] = useState(null);
  const [semester, setSemester] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (user?.userId) {
      fetchData();
    }
  }, [user, semester, activeTab]);

  const calculateHalfDayAttendancePercentage = (attendanceRecords = []) => {
    if (!Array.isArray(attendanceRecords) || attendanceRecords.length === 0) {
      return 0;
    }

    const totals = attendanceRecords.reduce(
      (acc, record) => {
        const session = (record?.session || '').toUpperCase();
        const weight = session === 'FN' || session === 'AN' ? 0.5 : 1;

        acc.total += weight;
        if ((record?.status || '').toUpperCase() === 'PRESENT') {
          acc.present += weight;
        }
        return acc;
      },
      { present: 0, total: 0 }
    );

    if (totals.total === 0) {
      return 0;
    }

    return Number(((totals.present / totals.total) * 100).toFixed(2));
  };

  const getAttendanceUnitSummary = (attendanceRecords = []) => {
    if (!Array.isArray(attendanceRecords) || attendanceRecords.length === 0) {
      return { presentUnits: 0, absentUnits: 0, totalUnits: 0 };
    }

    return attendanceRecords.reduce(
      (acc, record) => {
        const session = (record?.session || '').toUpperCase();
        const weight = session === 'FN' || session === 'AN' ? 0.5 : 1;
        const status = (record?.status || '').toUpperCase();

        acc.totalUnits += weight;
        if (status === 'PRESENT') {
          acc.presentUnits += weight;
        } else if (status === 'ABSENT') {
          acc.absentUnits += weight;
        }

        return acc;
      },
      { presentUnits: 0, absentUnits: 0, totalUnits: 0 }
    );
  };

  const getGroupedAttendanceByDate = (attendanceRecords = []) => {
    const grouped = new Map();

    (attendanceRecords || []).forEach((record) => {
      const dateKey =
        typeof record.attendanceDate === 'string'
          ? record.attendanceDate
          : new Date(record.attendanceDate).toISOString().split('T')[0];

      if (!grouped.has(dateKey)) {
        grouped.set(dateKey, { date: dateKey, fn: null, an: null, remarks: [] });
      }

      const row = grouped.get(dateKey);
      if ((record.session || '').toUpperCase() === 'FN') {
        row.fn = record;
      } else if ((record.session || '').toUpperCase() === 'AN') {
        row.an = record;
      }

      if (record.remarks) {
        row.remarks.push(record.remarks);
      }
    });

    return Array.from(grouped.values()).sort((a, b) => new Date(b.date) - new Date(a.date));
  };

  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      if (activeTab === 'attendance') {
        const endDate = new Date();
        const startDate = new Date();
        startDate.setDate(endDate.getDate() - 30);

        const [attendanceRes, percentageRes] = await Promise.all([
          attendanceAPI.getStudentAttendance(user.userId),
          attendanceAPI.getAttendancePercentage(
            user.userId,
            startDate.toISOString().split('T')[0],
            endDate.toISOString().split('T')[0]
          ),
        ]);
        const attendanceData = attendanceRes.data || [];
        setAttendance(attendanceData);

        const calculatedPercentage = calculateHalfDayAttendancePercentage(attendanceData);
        const fallbackApiPercentage = percentageRes.data?.attendancePercentage ?? 0;
        setAttendancePercentage(attendanceData.length > 0 ? calculatedPercentage : fallbackApiPercentage);
      } else if (activeTab === 'results') {
        const [semesterMarksResult, allMarksResult, cgpaResult] = await Promise.allSettled([
          marksAPI.getSemesterMarks(user.userId, semester),
          marksAPI.getStudentMarks(user.userId),
          marksAPI.calculateCGPA(user.userId),
        ]);

        if (semesterMarksResult.status === 'fulfilled') {
          const marksData = semesterMarksResult.value.data;
          setMarks(marksData ? (Array.isArray(marksData) ? marksData : [marksData]) : []);
        } else {
          console.error('Error fetching semester marks:', semesterMarksResult.reason);
          setMarks([]);
        }

        if (allMarksResult.status === 'fulfilled') {
          const allMarksData = allMarksResult.value.data;
          setAllSemesterMarks(Array.isArray(allMarksData) ? allMarksData : (allMarksData ? [allMarksData] : []));
        } else {
          console.error('Error fetching all semester marks:', allMarksResult.reason);
          setAllSemesterMarks([]);
        }

        if (cgpaResult.status === 'fulfilled') {
          setCGPA(cgpaResult.value.data?.cgpa || 0);
        } else {
          console.error('Error fetching CGPA:', cgpaResult.reason);
          setCGPA(0);
        }
      } else if (activeTab === 'courses') {
        const [selectedRes, allCoursesRes, statusRes] = await Promise.allSettled([
          courseAPI.getStudentCourses(user.userId, semester),
          courseAPI.getCoursesBySemester(semester),
          courseAPI.getCourseSelectionStatus(user.userId, semester),
        ]);

        if (selectedRes.status === 'fulfilled') {
          setSelectedCourses(selectedRes.value.data || []);
        } else {
          console.error('Error fetching selected courses:', selectedRes.reason);
          setSelectedCourses([]);
        }

        if (allCoursesRes.status === 'fulfilled') {
          setCourses(allCoursesRes.value.data || []);
        } else {
          console.error('Error fetching available courses:', allCoursesRes.reason);
          setCourses([]);
        }

        if (statusRes.status === 'fulfilled') {
          setSelectionStatus(statusRes.value.data || null);
        } else {
          console.error('Error fetching selection status:', statusRes.reason);
          setSelectionStatus(null);
        }
      }
    } catch (err) {
      console.error('Unexpected error fetching data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCourseSelect = async (courseId) => {
    try {
      const isSelected = selectedCourses.some((c) => c.courseId === courseId);
      if (isSelected) return;

      await courseAPI.selectCourse({
        studentUserId: user.userId,
        courseId,
        semester,
      });

      const [updatedCourses, updatedStatus] = await Promise.all([
        courseAPI.getStudentCourses(user.userId, semester),
        courseAPI.getCourseSelectionStatus(user.userId, semester),
      ]);
      setSelectedCourses(updatedCourses.data || []);
      setSelectionStatus(updatedStatus.data || null);
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to select course');
    }
  };

  const getAttendanceChartData = () => {
    const last7Days = attendance.slice(-7) || [];
    return {
      labels: last7Days.map((item) =>
        new Date(item.attendanceDate).toLocaleDateString('en-US', {
          month: 'short',
          day: 'numeric',
        })
      ),
      datasets: [
        {
          label: 'Attendance Status',
          data: last7Days.map((item) => (item.status === 'PRESENT' ? 1 : 0)),
          borderColor: '#10b981',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          borderWidth: 2,
          tension: 0.4,
        },
      ],
    };
  };

  const getMarksChartData = () => {
    if (!marks || marks.length === 0) {
      return {
        labels: [],
        datasets: [],
      };
    }

    const mark = marks[0];
    return {
      labels: ['Subject 1', 'Subject 2', 'Subject 3', 'Subject 4', 'Subject 5', 'Subject 6'],
      datasets: [
        {
          label: `Semester ${semester} Marks`,
          data: [
            mark.subject1Mark || 0,
            mark.subject2Mark || 0,
            mark.subject3Mark || 0,
            mark.subject4Mark || 0,
            mark.subject5Mark || 0,
            mark.subject6Mark || 0,
          ],
          backgroundColor: [
            '#3b82f6',
            '#10b981',
            '#f59e0b',
            '#ef4444',
            '#8b5cf6',
            '#ec4899',
          ],
          borderRadius: 8,
        },
      ],
    };
  };

  const getAttendanceStatusData = () => {
    const presentCount = attendance.filter((a) => a.status === 'PRESENT').length;
    const absentCount = attendance.filter((a) => a.status === 'ABSENT').length;

    return {
      labels: ['Present', 'Absent'],
      datasets: [
        {
          data: [presentCount, absentCount],
          backgroundColor: ['#10b981', '#ef4444'],
          borderColor: ['#059669', '#dc2626'],
          borderWidth: 2,
        },
      ],
    };
  };

  const getSemesterAnalysisChartData = () => {
    const validMarks = (allSemesterMarks || [])
      .filter((item) => item && item.id && item.semester)
      .sort((a, b) => a.semester - b.semester);

    if (validMarks.length === 0) {
      return {
        labels: [],
        datasets: [],
      };
    }

    return {
      labels: validMarks.map((item) => `Semester ${item.semester}`),
      datasets: [
        {
          label: 'SGPA Trend',
          data: validMarks.map((item) => item.sgpa || 0),
          borderColor: '#3b82f6',
          backgroundColor: 'rgba(59, 130, 246, 0.15)',
          borderWidth: 3,
          tension: 0.35,
          fill: true,
          pointRadius: 4,
          pointHoverRadius: 6,
        },
      ],
    };
  };

  const normalizeDepartment = (department) => (department || '').trim().toLowerCase();

  const normalizeCourseStatus = (status) =>
    (status || '')
      .trim()
      .toUpperCase()
      .replace(/[\s-]+/g, '_');

  const isCourseAllowedForStudent = (course) => {
    const studentDepartment = normalizeDepartment(user?.department);
    const courseDepartment = normalizeDepartment(course?.department);
    const courseStatus = normalizeCourseStatus(course?.courseStatus);

    if (!studentDepartment || !courseDepartment) {
      return true;
    }

    if (courseStatus === 'OPEN_ELECTIVE') {
      return studentDepartment !== courseDepartment;
    }

    if (courseStatus === 'CORE' || courseStatus === 'CORE_ELECTIVE' || courseStatus === 'ELECTIVE') {
      return studentDepartment === courseDepartment;
    }

    return true;
  };

  const canSelectByCurrentLimits = (course) => {
    if (!selectionStatus) {
      return true;
    }

    const status = normalizeCourseStatus(course?.courseStatus);

    if (status === 'CORE') {
      return (selectionStatus.coreCoursesSelected || 0) < (selectionStatus.maxCoreAllowed || 0);
    }

    if (status === 'CORE_ELECTIVE' || status === 'ELECTIVE') {
      return (selectionStatus.coreElectiveCoursesSelected || 0) < (selectionStatus.maxCoreElectiveAllowed || 0);
    }

    if (status === 'OPEN_ELECTIVE') {
      return (selectionStatus.openElectiveCoursesSelected || 0) < (selectionStatus.maxOpenElectiveAllowed || 0);
    }

    return true;
  };

  const isCourseSelected = (courseId) => selectedCourses.some((c) => c.courseId === courseId);

  const availableCourses = courses.filter(
    (course) =>
      isCourseAllowedForStudent(course) &&
      !isCourseSelected(course.courseId) &&
      canSelectByCurrentLimits(course)
  );

  const attendanceSummary = getAttendanceUnitSummary(attendance);
  const attendanceByDate = getGroupedAttendanceByDate(attendance);

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Student Dashboard</h1>
        <p>Welcome, {user?.username}!</p>
      </div>

      <div className="dashboard-tabs">
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
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="tab-content">
        {/* Attendance Tab */}
        {activeTab === 'attendance' && (
          <div className="tab-panel">
            <div className="stats-grid">
              <div className="stat-card">
                <h3>Attendance Percentage</h3>
                <p className="stat-value">{attendancePercentage}%</p>
              </div>
              <div className="stat-card">
                <h3>Total Days Attended</h3>
                <p className="stat-value">
                  {attendanceSummary.presentUnits}
                </p>
              </div>
              <div className="stat-card">
                <h3>Absent Days</h3>
                <p className="stat-value">
                  {attendanceSummary.absentUnits}
                </p>
              </div>
            </div>

            <div className="charts-grid attendance-charts-grid">
              <div className="chart-container attendance-chart-small">
                <h3>Attendance Trend</h3>
                {attendance.length > 0 ? (
                  <Line
                    data={getAttendanceChartData()}
                    options={{
                      responsive: true,
                      maintainAspectRatio: false,
                    }}
                  />
                ) : (
                  <p className="no-data">No attendance data available</p>
                )}
              </div>

              <div className="chart-container attendance-chart-small">
                <h3>Attendance Status</h3>
                {attendance.length > 0 ? (
                  <Doughnut
                    data={getAttendanceStatusData()}
                    options={{
                      responsive: true,
                      maintainAspectRatio: false,
                    }}
                  />
                ) : (
                  <p className="no-data">No attendance data available</p>
                )}
              </div>
            </div>

            <div className="history-section">
              <h3>Attendance History</h3>
              <div className="table-container">
                {attendance.length > 0 ? (
                  <table className="history-table">
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>FN</th>
                        <th>AN</th>
                        <th>Remarks</th>
                      </tr>
                    </thead>
                    <tbody>
                      {attendanceByDate.map((item) => (
                        <tr key={item.date}>
                          <td>{new Date(item.date).toLocaleDateString()}</td>
                          <td>
                            {item.fn ? (
                              <span className={`status-badge ${item.fn.status.toLowerCase()}`}>
                                {item.fn.status}
                              </span>
                            ) : (
                              '-'
                            )}
                          </td>
                          <td>
                            {item.an ? (
                              <span className={`status-badge ${item.an.status.toLowerCase()}`}>
                                {item.an.status}
                              </span>
                            ) : (
                              '-'
                            )}
                          </td>
                          <td>{item.remarks.length > 0 ? [...new Set(item.remarks)].join(', ') : '-'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                ) : (
                  <p className="no-data">No attendance records found</p>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Results Tab */}
        {activeTab === 'results' && (
          <div className="tab-panel">
            <div className="filter-section">
              <label htmlFor="semester">Select Semester:</label>
              <select
                id="semester"
                value={semester}
                onChange={(e) => setSemester(parseInt(e.target.value))}
              >
                {[1, 2, 3, 4, 5, 6, 7, 8].map((sem) => (
                  <option key={sem} value={sem}>
                    Semester {sem}
                  </option>
                ))}
              </select>
            </div>

            <div className="stats-grid">
              <div className="stat-card">
                <h3>CGPA</h3>
                <p className="stat-value">{typeof cgpa === 'number' ? cgpa.toFixed(2) : '0.00'}</p>
              </div>
              {marks.length > 0 && (
                <>
                  <div className="stat-card">
                    <h3>Total Marks</h3>
                    <p className="stat-value">{marks[0].totalMarks || 0}</p>
                  </div>
                  <div className="stat-card">
                    <h3>SGPA (Sem {semester})</h3>
                    <p className="stat-value">{marks[0].sgpa?.toFixed(2) || '0.00'}</p>
                  </div>
                </>
              )}
            </div>

            <div className="charts-grid">
              <div className="chart-container full-width">
                <h3>Subject-wise Marks</h3>
                {marks.length > 0 && marks[0].id ? (
                  <Bar data={getMarksChartData()} options={{ responsive: true }} />
                ) : (
                  <p className="no-data">No marks data available for this semester</p>
                )}
              </div>
              <div className="chart-container full-width">
                <h3>Semester-wise Analysis</h3>
                {allSemesterMarks.length > 0 ? (
                  <Line
                    data={getSemesterAnalysisChartData()}
                    options={{
                      responsive: true,
                      plugins: {
                        legend: { display: true },
                      },
                      scales: {
                        y: {
                          beginAtZero: true,
                          max: 10,
                          title: {
                            display: true,
                            text: 'SGPA',
                          },
                        },
                      },
                    }}
                  />
                ) : (
                  <p className="no-data">No semester-wise marks data available</p>
                )}
              </div>
            </div>

            <div className="results-section">
              <h3>Detailed Results</h3>
              {marks.length > 0 && marks[0].id ? (
                <div className="results-grid">
                  {['subject1', 'subject2', 'subject3', 'subject4', 'subject5', 'subject6'].map(
                    (subject, index) => (
                      <div key={index} className="result-card">
                        <h4>Subject {index + 1}</h4>
                        <p className="mark-value">{marks[0][`${subject}Mark`] || 'N/A'}</p>
                        <p className="mark-label">Marks</p>
                      </div>
                    )
                  )}
                </div>
              ) : (
                <p className="no-data">No results available for this semester</p>
              )}
            </div>
          </div>
        )}

        {/* Courses Tab */}
        {activeTab === 'courses' && (
          <div className="tab-panel">
            <div className="filter-section">
              <label htmlFor="course-semester">Select Semester:</label>
              <select
                id="course-semester"
                value={semester}
                onChange={(e) => setSemester(parseInt(e.target.value))}
              >
                {[1, 2, 3, 4, 5, 6, 7, 8].map((sem) => (
                  <option key={sem} value={sem}>
                    Semester {sem}
                  </option>
                ))}
              </select>
            </div>

            <div className="courses-info">
              <p>Rules: 4 Core + 2 Core Elective + 1 Open Elective (other department)</p>
              <p>
                Selected: <strong>{selectedCourses.length}/7</strong>
              </p>
              {selectionStatus && (
                <p>
                  Core: <strong>{selectionStatus.coreCoursesSelected}/{selectionStatus.maxCoreAllowed}</strong> | Core Elective: <strong>{selectionStatus.coreElectiveCoursesSelected}/{selectionStatus.maxCoreElectiveAllowed}</strong> | Open Elective: <strong>{selectionStatus.openElectiveCoursesSelected}/{selectionStatus.maxOpenElectiveAllowed}</strong>
                </p>
              )}
            </div>

            <div className="selected-courses">
              <h3>Selected Courses</h3>
              {selectedCourses.length > 0 ? (
                <>
                <div className="history-table" style={{ marginBottom: '10px' }}>
                  <table>
                    <thead>
                      <tr>
                        <th>Course Name</th>
                        <th>Course Type</th>
                      </tr>
                    </thead>
                  </table>
                </div>
                <div className="courses-list">
                  {selectedCourses.map((course) => (
                    <div key={course.courseId} className="course-item selected">
                      <span>{course.courseName}</span>
                      <span className={`course-type ${course.courseStatus?.toLowerCase()}`}>
                        {course.courseStatus}
                      </span>
                    </div>
                  ))}
                </div>
                </>
              ) : (
                <p className="no-data">No courses selected yet</p>
              )}
            </div>

            <div className="available-courses">
              <h3>Available Courses</h3>
              {availableCourses.length > 0 ? (
                <>
                <div className="history-table" style={{ marginBottom: '10px' }}>
                  <table>
                    <thead>
                      <tr>
                        <th>Course Name</th>
                        <th>Type</th>
                        <th>Code</th>
                        <th>Faculty</th>
                        <th>Action</th>
                      </tr>
                    </thead>
                  </table>
                </div>
                <div className="courses-grid">
                  {availableCourses.map((course) => (
                    <div
                      key={course.courseId}
                      className="course-card"
                    >
                      <div className="course-header">
                        <h4>{course.courseName}</h4>
                        <span className={`course-badge ${course.courseStatus?.toLowerCase()}`}>
                          {course.courseStatus}
                        </span>
                      </div>
                      <p className="course-code">{course.courseCode}</p>
                      <p className="course-faculty">Department: {course.department}</p>
                      <button
                        className="btn-select-course"
                        onClick={() => handleCourseSelect(course.courseId)}
                      >
                        Select
                      </button>
                    </div>
                  ))}
                </div>
                </>
              ) : (
                <p className="no-data">No courses available</p>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default StudentDashboard;
