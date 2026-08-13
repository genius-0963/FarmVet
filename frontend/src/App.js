import { Routes, Route, NavLink, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Home from './pages/Home';
import Register from './pages/Register';
import Login from './pages/Login';
import Crops from './pages/Crops';
import Pesticides from './pages/Pesticides';
import Veterinarians from './pages/Veterinarians';
import { ProtectedRoute } from './components/ProtectedRoute';
import Button from './components/ui/Button';

function Navbar() {
  const { user, logout } = useAuth();
  const location = useLocation();

  const navLinkClass = ({ isActive }) => 
    `px-3 py-2 rounded-md text-sm font-medium transition-colors ${
      isActive 
        ? 'bg-primary-100 text-primary-700' 
        : 'text-gray-700 hover:bg-gray-100'
    }`;

  const isAuthPage = location.pathname === '/login' || location.pathname === '/register';

  if (isAuthPage) return null;

  return (
    <nav className="bg-white shadow-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex">
            <div className="flex-shrink-0 flex items-center">
              <NavLink to="/" className="flex items-center gap-2">
                <div className="bg-primary-600 rounded-full p-2">
                  <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                  </svg>
                </div>
                <span className="text-xl font-bold text-gray-900">FarmVet Connect</span>
              </NavLink>
            </div>
            <div className="hidden sm:ml-6 sm:flex sm:space-x-8">
              <NavLink to="/" className={navLinkClass}>
                Home
              </NavLink>
              {user && (
                <>
                  <NavLink to="/crops" className={navLinkClass}>
                    Crops
                  </NavLink>
                  <NavLink to="/pesticides" className={navLinkClass}>
                    Pesticides
                  </NavLink>
                  <NavLink to="/veterinarians" className={navLinkClass}>
                    Veterinarians
                  </NavLink>
                </>
              )}
            </div>
          </div>
          <div className="flex items-center">
            {!user ? (
              <div className="flex space-x-4">
                <NavLink to="/login" className={navLinkClass}>
                  Login
                </NavLink>
                <NavLink to="/register">
                  <Button variant="primary" size="sm">
                    Register
                  </Button>
                </NavLink>
              </div>
            ) : (
              <div className="flex items-center space-x-4">
                <div className="flex items-center">
                  <div className="bg-primary-100 rounded-full p-2 mr-2">
                    <svg className="w-5 h-5 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                  </div>
                  <div className="text-sm">
                    <p className="font-medium text-gray-900">{user.username}</p>
                    <p className="text-xs text-gray-500">{user.role}</p>
                  </div>
                </div>
                <Button variant="outline" size="sm" onClick={logout}>
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
            <ProtectedRoute>
              <Crops />
            </ProtectedRoute>
          } />
          <Route path="/pesticides" element={
            <ProtectedRoute>
              <Pesticides />
            </ProtectedRoute>
          } />
          <Route path="/veterinarians" element={
            <ProtectedRoute>
              <Veterinarians />
            </ProtectedRoute>
          } />
        </Routes>
      </main>
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;
