import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Alert from '../components/ui/Alert';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', password: '' });
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage('');
    setLoading(true);

    try {
      const response = await axios.post(`${API_BASE}/v1/auth/login`, form);
      const resData = response.data;
      if (resData.success && resData.data) {
        login(resData.data.token, resData.data);
        setMessage(`Welcome back, ${resData.data.username}!`);
        setMessageType('success');
        setTimeout(() => navigate('/'), 400);
      } else {
        throw new Error(resData.message || 'Login failed');
      }
    } catch (error) {
      const errorMessage = error.response?.data?.message || (error.response?.status === 401 
        ? 'Invalid username or password' 
        : 'Login failed. Please verify server connection.');
      setMessage(errorMessage);
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-emerald-50 to-green-100 px-4 py-8">
      <div className="max-w-md w-full">
        <div className="flex justify-center mb-6">
          <div className="bg-emerald-600 rounded-full p-4 shadow-lg text-white">
            <svg className="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
            </svg>
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-xl p-8 border border-gray-100">
          <div className="text-center mb-8">
            <h2 className="text-3xl font-extrabold text-gray-900 mb-1">FarmVet Connect</h2>
            <p className="text-sm text-gray-600">Agricultural & Veterinary Intelligence Portal</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Username"
              name="username"
              value={form.username}
              onChange={handleChange}
              required
              placeholder="e.g. ramesh_patil"
              autoComplete="username"
            />

            <Input
              label="Password"
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              required
              placeholder="••••••••"
              autoComplete="current-password"
            />

            <Button
              type="submit"
              variant="primary"
              size="lg"
              loading={loading}
              className="w-full mt-6 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold py-3 rounded-xl shadow-md transition-all"
            >
              {loading ? 'Authenticating...' : 'Sign In'}
            </Button>
          </form>

          {message && (
            <Alert 
              type={messageType} 
              message={message}
              onClose={() => setMessage('')}
              className="mt-4"
            />
          )}

          <div className="mt-6 text-center text-sm text-gray-600">
            Don't have an account?{' '}
            <Link to="/register" className="text-emerald-600 font-semibold hover:underline">
              Register as Farmer or Expert
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
