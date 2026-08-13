import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Select from '../components/ui/Select';
import Alert from '../components/ui/Alert';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

const roleOptions = [
  { value: 'USER', label: 'User' },
  { value: 'FARMER', label: 'Farmer' },
  { value: 'VET', label: 'Veterinarian' }
];

export default function Register() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', phone: '', password: '', role: 'USER' });
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage('');
    setLoading(true);
    try {
      await axios.post(`${API_BASE}/auth/register`, form);
      const userData = { username: form.username, email: form.email, role: form.role };
      login(userData);
      setMessage('Registration successful! Welcome to FarmVet.');
      setMessageType('success');
      navigate('/');
    } catch (error) {
      setMessage(error.response?.data || 'Registration failed. Please try again.');
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-secondary-50 px-4 py-8">
      <div className="max-w-lg w-full">
        {/* Logo/Icon */}
        <div className="flex justify-center mb-8">
          <div className="bg-secondary-600 rounded-full p-4 shadow-lg">
            <svg className="w-12 h-12 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
            </svg>
          </div>
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-xl p-8">
          <div className="text-center mb-8">
            <h2 className="text-3xl font-bold text-gray-900 mb-2">Create Account</h2>
            <p className="text-gray-600">Join FarmVet Connect today</p>
          </div>

          <form onSubmit={handleSubmit}>
            <Input
              label="Username"
              name="username"
              value={form.username}
              onChange={handleChange}
              required
              placeholder="Choose a username"
              autoComplete="username"
            />

            <Input
              label="Email"
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              required
              placeholder="Enter your email"
              autoComplete="email"
            />

            <Input
              label="Phone"
              type="tel"
              name="phone"
              value={form.phone}
              onChange={handleChange}
              required
              placeholder="Enter your phone number"
              autoComplete="tel"
            />

            <Input
              label="Password"
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              required
              placeholder="Create a password"
              autoComplete="new-password"
            />

            <Select
              label="Role"
              name="role"
              value={form.role}
              onChange={handleChange}
              options={roleOptions}
              required
            />

            <Button
              type="submit"
              variant="primary"
              size="lg"
              loading={loading}
              className="w-full mt-6"
            >
              {loading ? 'Creating Account...' : 'Register'}
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

          <div className="mt-6 text-center">
            <p className="text-gray-600">
              Already have an account?{' '}
              <Link to="/login" className="text-primary-600 hover:text-primary-700 font-medium">
                Login here
              </Link>
            </p>
          </div>
        </div>

        {/* Footer */}
        <div className="mt-8 text-center text-sm text-gray-500">
          <p>FarmVet Connect - Agricultural Management System</p>
        </div>
      </div>
    </div>
  );
}
