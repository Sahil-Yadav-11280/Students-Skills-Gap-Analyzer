import { Search, Download, Plus } from 'lucide-react';
import { dashboardService } from '../services/api';

export default function Header({ selectedStudentId }) {
  
  const handleExport = () => {
    if(selectedStudentId) {
      dashboardService.exportDashboard(selectedStudentId);
    }
  };

  return (
    <div className="h-24 px-8 flex items-center justify-between border-b border-gray-800 bg-dashBg">
      
      {/* Navigation Tabs */}
      <div className="flex gap-8 text-sm font-medium text-gray-400">
        <button className="text-white border-b-2 border-accentPurple pb-1">Dashboard</button>
        <button className="hover:text-white transition-colors">Skill Map</button>
        <button className="hover:text-white transition-colors">Predictions</button>
        <button className="hover:text-white transition-colors">Cohort View</button>
        <button className="bg-cardBg px-4 py-1.5 rounded-full text-accentPurple">Reports</button>
      </div>

      {/* Actions: Search, Export, Add */}
      <div className="flex items-center gap-4">
        
        {/* Search Bar */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" size={16} />
          <input 
            type="text" 
            placeholder="Search student ID..." 
            className="bg-cardBg text-sm text-white pl-10 pr-4 py-2 rounded-full border border-gray-700 focus:outline-none focus:border-accentPurple w-64"
          />
        </div>

        {/* Export Button */}
        <button onClick={handleExport} className="flex items-center gap-2 bg-cardBg text-gray-300 border border-gray-700 px-4 py-2 rounded-full text-sm font-medium hover:bg-gray-800 transition-colors">
          <Download size={16} />
          Export
        </button>

        {/* New Analysis Button */}
        <button className="flex items-center gap-2 bg-accentPurple text-white px-5 py-2 rounded-full text-sm font-semibold hover:bg-purple-500 transition-colors shadow-lg shadow-purple-500/20">
          <Plus size={16} />
          New Analysis
        </button>
      </div>
    </div>
  );
}