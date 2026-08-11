import { useEffect, useState } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function Pesticides() {
  const { user } = useAuth();
  const [pesticides, setPesticides] = useState([]);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [editingPesticide, setEditingPesticide] = useState(null);
  const [form, setForm] = useState({
    farmerId: '',
    cropName: '',
    acres: '',
    diseaseName: '',
    pesticideName: '',
    pesticideStatus: '',
    pesticideQuantity: ''
  });

  const fetchPesticides = async () => {
    setLoading(true);
    try {
      const url = user?.role === 'FARMER' && user?.id 
        ? `${API_BASE}/pesticides/farmer/${user.id}`
        : `${API_BASE}/pesticides`;
      const response = await axios.get(url);
      setPesticides(response.data);
      setMessage('');
    } catch (error) {
      setMessage('Unable to load pesticides');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPesticides();
  }, [user]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const pesticideData = { ...form, farmerId: user?.id || form.farmerId };
      if (editingPesticide) {
        await axios.put(`${API_BASE}/pesticides/${editingPesticide.id}`, pesticideData);
        setMessage('Pesticide updated successfully');
      } else {
        await axios.post(`${API_BASE}/pesticides`, pesticideData);
        setMessage('Pesticide added successfully');
      }
      setShowForm(false);
      setEditingPesticide(null);
      setForm({
        farmerId: '',
        cropName: '',
        acres: '',
        diseaseName: '',
        pesticideName: '',
        pesticideStatus: '',
        pesticideQuantity: ''
      });
      fetchPesticides();
    } catch (error) {
      setMessage(editingPesticide ? 'Failed to update pesticide' : 'Failed to add pesticide');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (pesticide) => {
    setEditingPesticide(pesticide);
    setForm(pesticide);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this pesticide record?')) return;
    setLoading(true);
    try {
      await axios.delete(`${API_BASE}/pesticides/${id}`);
      setMessage('Pesticide deleted successfully');
      fetchPesticides();
    } catch (error) {
      setMessage('Failed to delete pesticide');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async (id, newStatus) => {
    setLoading(true);
    try {
      await axios.patch(`${API_BASE}/pesticides/${id}/status`, newStatus, {
        headers: { 'Content-Type': 'application/json' }
      });
      setMessage('Status updated successfully');
      fetchPesticides();
    } catch (error) {
      setMessage('Failed to update status');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingPesticide(null);
    setForm({
      farmerId: '',
      cropName: '',
      acres: '',
      diseaseName: '',
      pesticideName: '',
      pesticideStatus: '',
      pesticideQuantity: ''
    });
  };

  return (
    <div>
      <h2>Pesticides</h2>
      {message && <p style={{ color: message.includes('success') ? 'green' : 'red' }}>{message}</p>}
      
      {(user?.role === 'FARMER' || user?.role === 'USER') && (
        <button onClick={() => setShowForm(true)} style={{ marginBottom: '20px' }}>
          {editingPesticide ? 'Edit Pesticide' : 'Add New Pesticide'}
        </button>
      )}

      {showForm && (
        <div style={{ border: '1px solid #ccc', padding: '20px', marginBottom: '20px' }}>
          <h3>{editingPesticide ? 'Edit Pesticide' : 'Add New Pesticide'}</h3>
          <form onSubmit={handleSubmit}>
            <label>Crop Name</label>
            <input name="cropName" value={form.cropName} onChange={handleChange} required /><br/>
            <label>Acres</label>
            <input name="acres" value={form.acres} onChange={handleChange} required /><br/>
            <label>Disease Name</label>
            <input name="diseaseName" value={form.diseaseName} onChange={handleChange} /><br/>
            <label>Pesticide Name</label>
            <input name="pesticideName" value={form.pesticideName} onChange={handleChange} required /><br/>
            <label>Pesticide Quantity</label>
            <input name="pesticideQuantity" value={form.pesticideQuantity} onChange={handleChange} required /><br/>
            <label>Status</label>
            <select name="pesticideStatus" value={form.pesticideStatus} onChange={handleChange}>
              <option value="">Select Status</option>
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
            </select><br/>
            <button type="submit" disabled={loading}>
              {loading ? 'Saving...' : (editingPesticide ? 'Update' : 'Add')}
            </button>
            <button type="button" onClick={handleCancel} style={{ marginLeft: '10px' }}>
              Cancel
            </button>
          </form>
        </div>
      )}

      {loading && <p>Loading...</p>}
      
      {!loading && pesticides.length === 0 ? (
        <p>No pesticide records found.</p>
      ) : (
        <ul>
          {pesticides.map(pesticide => (
            <li key={pesticide.id} style={{ marginBottom: '15px', border: '1px solid #eee', padding: '10px' }}>
              <strong>{pesticide.pesticideName}</strong> for {pesticide.cropName} ({pesticide.pesticideQuantity})<br/>
              Disease: {pesticide.diseaseName} | Acres: {pesticide.acres}<br/>
              Status: <span style={{ 
                color: pesticide.pesticideStatus === 'APPROVED' ? 'green' : 
                      pesticide.pesticideStatus === 'REJECTED' ? 'red' : 'orange'
              }}>{pesticide.pesticideStatus}</span><br/>
              {(user?.role === 'FARMER' || user?.role === 'USER') && (
                <>
                  <button onClick={() => handleEdit(pesticide)} style={{ marginRight: '10px' }}>Edit</button>
                  <button onClick={() => handleDelete(pesticide.id)} style={{ marginRight: '10px' }}>Delete</button>
                  <select 
                    value={pesticide.pesticideStatus} 
                    onChange={(e) => handleStatusUpdate(pesticide.id, e.target.value)}
                    style={{ marginLeft: '10px' }}
                  >
                    <option value="PENDING">Pending</option>
                    <option value="APPROVED">Approved</option>
                    <option value="REJECTED">Rejected</option>
                  </select>
                </>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
