import { useEffect, useState } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function Veterinarians() {
  const { user } = useAuth();
  const [vets, setVets] = useState([]);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [editingVet, setEditingVet] = useState(null);
  const [form, setForm] = useState({
    username: '',
    position: '',
    experience: '',
    about: '',
    speciality: '',
    email: '',
    phone: '',
    password: ''
  });

  const fetchVets = async () => {
    setLoading(true);
    try {
      const response = await axios.get(`${API_BASE}/veterinarians`);
      setVets(response.data);
      setMessage('');
    } catch (error) {
      setMessage('Unable to load veterinarians');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVets();
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (editingVet) {
        await axios.put(`${API_BASE}/veterinarians/${editingVet.id}`, form);
        setMessage('Veterinarian updated successfully');
      } else {
        await axios.post(`${API_BASE}/veterinarians`, form);
        setMessage('Veterinarian added successfully');
      }
      setShowForm(false);
      setEditingVet(null);
      setForm({
        username: '',
        position: '',
        experience: '',
        about: '',
        speciality: '',
        email: '',
        phone: '',
        password: ''
      });
      fetchVets();
    } catch (error) {
      setMessage(editingVet ? 'Failed to update veterinarian' : 'Failed to add veterinarian');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (vet) => {
    setEditingVet(vet);
    setForm(vet);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this veterinarian?')) return;
    setLoading(true);
    try {
      await axios.delete(`${API_BASE}/veterinarians/${id}`);
      setMessage('Veterinarian deleted successfully');
      fetchVets();
    } catch (error) {
      setMessage('Failed to delete veterinarian');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingVet(null);
    setForm({
      username: '',
      position: '',
      experience: '',
      about: '',
      speciality: '',
      email: '',
      phone: '',
      password: ''
    });
  };

  return (
    <div>
      <h2>Veterinarians</h2>
      {message && <p style={{ color: message.includes('success') ? 'green' : 'red' }}>{message}</p>}
      
      {(user?.role === 'VET' || user?.role === 'USER') && (
        <button onClick={() => setShowForm(true)} style={{ marginBottom: '20px' }}>
          {editingVet ? 'Edit Veterinarian' : 'Add New Veterinarian'}
        </button>
      )}

      {showForm && (
        <div style={{ border: '1px solid #ccc', padding: '20px', marginBottom: '20px' }}>
          <h3>{editingVet ? 'Edit Veterinarian' : 'Add New Veterinarian'}</h3>
          <form onSubmit={handleSubmit}>
            <label>Username</label>
            <input name="username" value={form.username} onChange={handleChange} required /><br/>
            <label>Position</label>
            <input name="position" value={form.position} onChange={handleChange} /><br/>
            <label>Experience (years)</label>
            <input name="experience" value={form.experience} onChange={handleChange} /><br/>
            <label>Speciality</label>
            <input name="speciality" value={form.speciality} onChange={handleChange} /><br/>
            <label>Email</label>
            <input type="email" name="email" value={form.email} onChange={handleChange} /><br/>
            <label>Phone</label>
            <input name="phone" value={form.phone} onChange={handleChange} /><br/>
            <label>About</label>
            <textarea name="about" value={form.about} onChange={handleChange} rows="3" /><br/>
            {!editingVet && (
              <>
                <label>Password</label>
                <input type="password" name="password" value={form.password} onChange={handleChange} required /><br/>
              </>
            )}
            <button type="submit" disabled={loading}>
              {loading ? 'Saving...' : (editingVet ? 'Update' : 'Add')}
            </button>
            <button type="button" onClick={handleCancel} style={{ marginLeft: '10px' }}>
              Cancel
            </button>
          </form>
        </div>
      )}

      {loading && <p>Loading...</p>}
      
      {!loading && vets.length === 0 ? (
        <p>No veterinarians found.</p>
      ) : (
        <ul>
          {vets.map(vet => (
            <li key={vet.id} style={{ marginBottom: '15px', border: '1px solid #eee', padding: '10px' }}>
              <strong>{vet.username}</strong> - {vet.speciality} ({vet.experience} years)<br/>
              Position: {vet.position} | Email: {vet.email}<br/>
              Phone: {vet.phone}<br/>
              {vet.about && <small>About: {vet.about}</small>}<br/>
              {(user?.role === 'VET' || user?.role === 'USER') && (
                <>
                  <button onClick={() => handleEdit(vet)} style={{ marginRight: '10px' }}>Edit</button>
                  <button onClick={() => handleDelete(vet.id)}>Delete</button>
                </>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
