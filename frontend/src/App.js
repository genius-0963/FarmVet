import { Routes, Route, NavLink } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Home from './pages/Home';
import Register from './pages/Register';
import Login from './pages/Login';
import Crops from './pages/Crops';
import Pesticides from './pages/Pesticides';
import Veterinarians from './pages/Veterinarians';
import { ProtectedRoute } from './components/ProtectedRoute';
import './App.css';

function AppContent() {
  const { user, logout } = useAuth();

  return (
    <div className="App">
      <header className="App-header">
        <h1>FarmVet Connect</h1>
        <nav>
          <NavLink to="/">Home</NavLink>
          {!user && <NavLink to="/register">Register</NavLink>}
          {!user && <NavLink to="/login">Login</NavLink>}
          {user && <NavLink to="/crops">Crops</NavLink>}
          {user && <NavLink to="/pesticides">Pesticides</NavLink>}
          {user && <NavLink to="/veterinarians">Veterinarians</NavLink>}
          {user && <button onClick={logout} style={{ marginLeft: '10px' }}>Logout</button>}
        </nav>
        {user && <p>Welcome, {user.username} ({user.role})</p>}
      </header>
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
