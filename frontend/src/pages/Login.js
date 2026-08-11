import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', password: '' });
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage('');
    setLoading(true);
    try {
      const response = await axios.post(`${API_BASE}/auth/login`, form);
      login(response.data);
      setMessage(`Welcome ${response.data.username}`);
      navigate('/');
    } catch (error) {
      setMessage(error.response?.status === 401 ? 'Invalid credentials' : 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2>Login</h2>
      <form onSubmit={handleSubmit}>
        <label>Username</label>
        <input name="username" value={form.username} onChange={handleChange} required />
        <label>Password</label>
        <input type="password" name="password" value={form.password} onChange={handleChange} required />
        <button type="submit" disabled={loading}>{loading ? 'Logging in...' : 'Login'}</button>
      </form>
      {message && <p>{message}</p>}
    </div>
  );
}
