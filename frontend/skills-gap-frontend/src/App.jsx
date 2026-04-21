import { useState } from 'react';
import Sidebar from './components/Sidebar';
import Header from './components/Header';

function App() {
  const [selectedStudentId, setSelectedStudentId] = useState(null);

  return (
    <div className="flex h-screen bg-dashBg text-white overflow-hidden font-sans">
      
      {/* LEFT: Sidebar */}
      <Sidebar 
        selectedStudentId={selectedStudentId} 
        onSelectStudent={setSelectedStudentId} 
      />

      {/* RIGHT: Main Content Area */}
      <div className="flex-1 flex flex-col h-screen overflow-hidden">
        
        {/* Top Header Navigation */}
        <Header selectedStudentId={selectedStudentId} />

        {/* Main Dashboard Canvas (Scrollable) */}
        <div className="flex-1 overflow-y-auto p-8">
          
          {selectedStudentId ? (
             <div className="border-2 border-dashed border-gray-700 rounded-xl h-full flex items-center justify-center text-gray-500">
               {/* WE WILL PUT THE CHARTS AND CARDS HERE NEXT! */}
               <h1>Dashboard components for Student ID: {selectedStudentId} will go here</h1>
             </div>
          ) : (
            <div className="h-full flex items-center justify-center text-gray-500">
               Select a student from the sidebar to view their dashboard.
            </div>
          )}

        </div>
      </div>
    </div>
  );
}

export default App;