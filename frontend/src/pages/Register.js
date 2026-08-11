import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function Register() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', phone: '', password: '', role: 'USER' });
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage('');
    setLoading(true);
    try {
      const response = await axios.post(`${API_BASE}/auth/register`, form);
      const userData = { username: form.username, email: form.email, role: form.role };
      login(userData);
      setMessage(response.data || 'Registration success');
      navigate('/');
    } catch (error) {
      setMessage(error.response?.data || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2>Register</h2>
      <form onSubmit={handleSubmit}>
        <label>Username</label>
        <input name="username" value={form.username} onChange={handleChange} required />
        <label>Email</label>
        <input type="email" name="email" value={form.email} onChange={handleChange} required />
        <label>Phone</label>
        <input name="phone" value={form.phone} onChange={handleChange} required />
        <label>Password</label>
        <input type="password" name="password" value={form.password} onChange={handleChange} required />
        <label>Role</label>
        <select name="role" value={form.role} onChange={handleChange}>
          <option value="USER">User</option>
          <option value="FARMER">Farmer</option>
          <option value="VET">Veterinarian</option>
        </select>
        <button type="submit" disabled={loading}>{loading ? 'Registering...' : 'Register'}</button>
      </form>
      {message && <p>{message}</p>}
    </div>
  );
}
