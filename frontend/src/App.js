import { Routes, Route, NavLink, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Home from './pages/Home';
import Register from './pages/Register';
import Login from './pages/Login';
import Crops from './pages/Crops';
import Pesticides from './pages/Pesticides';
import Veterinarians from './pages/Veterinarians';
import DiseaseDiagnosis from './pages/DiseaseDiagnosis';
import IrrigationAdvisor from './pages/IrrigationAdvisor';
import MandiAndFinance from './pages/MandiAndFinance';
import OutbreakRadar from './pages/OutbreakRadar';
import SchemeFinder from './pages/SchemeFinder';
import Livestock from './pages/Livestock';
import Consultations from './pages/Consultations';
import AiAssistant from './pages/AiAssistant';
import FloatingChatbotAgent from './components/FloatingChatbotAgent';
import { ProtectedRoute } from './components/ProtectedRoute';
import Button from './components/ui/Button';

function Navbar() {
  const { user, logout } = useAuth();
  const location = useLocation();

  const navLinkClass = ({ isActive }) => 
    `px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition-colors ${
      isActive 
        ? 'bg-emerald-600 text-white shadow-sm' 
        : 'text-gray-700 hover:bg-emerald-50 hover:text-emerald-700'
    }`;

  const isAuthPage = location.pathname === '/login' || location.pathname === '/register';
  if (isAuthPage) return null;

  return (
    <nav className="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center gap-6 overflow-x-auto py-2">
            <NavLink to="/" className="flex items-center gap-2 shrink-0">
              <div className="bg-emerald-600 rounded-xl p-2 shadow-sm text-white">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                </svg>
              </div>
              <span className="text-lg font-black text-gray-900 tracking-tight">FarmVet</span>
            </NavLink>

            {user && (
              <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar">
                <NavLink to="/" className={navLinkClass}>Home</NavLink>
                <NavLink to="/crops" className={navLinkClass}>Crops</NavLink>
                <NavLink to="/disease-diagnosis" className={navLinkClass}>🔬 Disease AI</NavLink>
                <NavLink to="/pesticides" className={navLinkClass}>🧪 Pesticides</NavLink>
                <NavLink to="/irrigation" className={navLinkClass}>💧 Irrigation</NavLink>
                <NavLink to="/finance" className={navLinkClass}>🌾 Mandi & Finance</NavLink>
                <NavLink to="/radar" className={navLinkClass}>🚨 Outbreak Radar</NavLink>
                <NavLink to="/schemes" className={navLinkClass}>🏛️ Schemes</NavLink>
                <NavLink to="/livestock" className={navLinkClass}>🐄 Livestock</NavLink>
                <NavLink to="/consultations" className={navLinkClass}>🩺 Consultations</NavLink>
                <NavLink to="/ai-assistant" className={navLinkClass}>🤖 AI Assistant</NavLink>
              </div>
            )}
          </div>

          <div className="flex items-center shrink-0 ml-4">
            {!user ? (
              <div className="flex space-x-2">
                <NavLink to="/login">
                  <Button variant="outline" size="sm">Login</Button>
                </NavLink>
                <NavLink to="/register">
                  <Button variant="primary" size="sm" className="bg-emerald-600 hover:bg-emerald-700 text-white">Register</Button>
                </NavLink>
              </div>
            ) : (
              <div className="flex items-center space-x-3">
                <div className="text-right hidden sm:block">
                  <p className="text-xs font-bold text-gray-900">{user.username}</p>
                  <p className="text-[10px] text-gray-500 uppercase">{user.role}</p>
                </div>
                <Button variant="outline" size="sm" onClick={logout} className="text-xs">
                  Logout
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}

function AppContent() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/register" element={<Register />} />
          <Route path="/login" element={<Login />} />
          <Route path="/crops" element={
            <ProtectedRoute><Crops /></ProtectedRoute>
          } />
          <Route path="/disease-diagnosis" element={
            <ProtectedRoute><DiseaseDiagnosis /></ProtectedRoute>
          } />
          <Route path="/pesticides" element={
            <ProtectedRoute><Pesticides /></ProtectedRoute>
          } />
          <Route path="/irrigation" element={
            <ProtectedRoute><IrrigationAdvisor /></ProtectedRoute>
          } />
          <Route path="/finance" element={
            <ProtectedRoute><MandiAndFinance /></ProtectedRoute>
          } />
          <Route path="/radar" element={
            <ProtectedRoute><OutbreakRadar /></ProtectedRoute>
          } />
          <Route path="/schemes" element={
            <ProtectedRoute><SchemeFinder /></ProtectedRoute>
          } />
          <Route path="/livestock" element={
            <ProtectedRoute><Livestock /></ProtectedRoute>
          } />
          <Route path="/consultations" element={
            <ProtectedRoute><Consultations /></ProtectedRoute>
          } />
          <Route path="/veterinarians" element={
            <ProtectedRoute><Veterinarians /></ProtectedRoute>
          } />
          <Route path="/ai-assistant" element={
            <ProtectedRoute><AiAssistant /></ProtectedRoute>
          } />
        </Routes>
      </main>
      <FloatingChatbotAgent />
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}
