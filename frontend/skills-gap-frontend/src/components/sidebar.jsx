import { useState, useEffect } from 'react';
import { Search, Zap } from 'lucide-react';
import { dashboardService } from '../services/api';

export default function Sidebar({ selectedStudentId, onSelectStudent }) {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fetch students from Spring Boot when the sidebar loads
    dashboardService.getAllStudents()
      .then(data => {
        setStudents(data);
        setLoading(false);
        // Auto-select the first student if none is selected
        if (data.length > 0 && !selectedStudentId) {
          onSelectStudent(data[0].id);
        }
      })
      .catch(err => console.error("Failed to load students", err));
  }, [selectedStudentId, onSelectStudent]);

  return (
    <div className="w-80 bg-dashBg border-r border-gray-800 h-screen flex flex-col">
      {/* Logo Area */}
      <div className="p-6 flex items-center gap-3">
        <div className="bg-gradient-to-br from-orange-400 to-accentPurple p-2 rounded-lg text-white">
          <Zap size={24} fill="currentColor" />
        </div>
        <div>
          <h1 className="text-xl font-bold text-white tracking-wide">SkillGap AI</h1>
          <p className="text-xs text-gray-400">ASSISTMENTS ANALYTICS PLATFORM</p>
        </div>
      </div>

      {/* STUDENTS LIST TITLE */}
      <div className="px-6 py-2 text-xs font-bold text-gray-500 tracking-wider">
        STUDENTS
      </div>

      {/* Student List */}
      <div className="flex-1 overflow-y-auto px-4 space-y-2 pb-4">
        {loading ? (
          <div className="text-gray-500 p-2 text-sm">Loading students...</div>
        ) : (
          students.map((student) => {
            const isActive = selectedStudentId === student.id;
            // Create initials (e.g., "Arjun Mehta" -> "AM")
            const initials = student.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();

            return (
              <button
                key={student.id}
                onClick={() => onSelectStudent(student.id)}
                className={`w-full flex items-center justify-between p-3 rounded-xl transition-all duration-200 ${
                  isActive ? 'bg-cardBg border border-accentPurple/50' : 'hover:bg-cardBg border border-transparent'
                }`}
              >
                <div className="flex items-center gap-3">
                  {/* Avatar */}
                  <div className={`w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm ${
                    isActive ? 'bg-accentPurple/20 text-accentPurple' : 'bg-gray-800 text-gray-400'
                  }`}>
                    {initials}
                  </div>
                  {/* Name & ID */}
                  <div className="text-left">
                    <p className={`font-semibold text-sm ${isActive ? 'text-white' : 'text-gray-300'}`}>
                      {student.name}
                    </p>
                    <p className="text-xs text-gray-500">{student.code}</p>
                  </div>
                </div>
                {/* Score */}
                <span className={`text-sm font-bold ${isActive ? 'text-successGreen' : 'text-gray-500'}`}>
                  {student.score}%
                </span>
              </button>
            );
          })
        )}
      </div>
    </div>
  );
}