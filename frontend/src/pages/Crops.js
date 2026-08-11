import { useEffect, useState } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function Crops() {
  const { user } = useAuth();
  const [crops, setCrops] = useState([]);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [editingCrop, setEditingCrop] = useState(null);
  const [form, setForm] = useState({
    farmerId: '',
    cropName: '',
    acres: '',
    location: '',
    soilType: '',
    startMonth: '',
    endMonth: '',
    manager: '',
    contact: ''
  });

  const fetchCrops = async () => {
    setLoading(true);
    try {
      const url = user?.role === 'FARMER' && user?.id 
        ? `${API_BASE}/crops/farmer/${user.id}`
        : `${API_BASE}/crops`;
      const response = await axios.get(url);
      setCrops(response.data);
      setMessage('');
    } catch (error) {
      setMessage('Unable to load crops');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCrops();
  }, [user]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const cropData = { ...form, farmerId: user?.id || form.farmerId };
      if (editingCrop) {
        await axios.put(`${API_BASE}/crops/${editingCrop.id}`, cropData);
        setMessage('Crop updated successfully');
      } else {
        await axios.post(`${API_BASE}/crops`, cropData);
        setMessage('Crop added successfully');
      }
      setShowForm(false);
      setEditingCrop(null);
      setForm({
        farmerId: '',
        cropName: '',
        acres: '',
        location: '',
        soilType: '',
        startMonth: '',
        endMonth: '',
        manager: '',
        contact: ''
      });
      fetchCrops();
    } catch (error) {
      setMessage(editingCrop ? 'Failed to update crop' : 'Failed to add crop');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (crop) => {
    setEditingCrop(crop);
    setForm(crop);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this crop?')) return;
    setLoading(true);
    try {
      await axios.delete(`${API_BASE}/crops/${id}`);
      setMessage('Crop deleted successfully');
      fetchCrops();
    } catch (error) {
      setMessage('Failed to delete crop');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingCrop(null);
    setForm({
      farmerId: '',
      cropName: '',
      acres: '',
      location: '',
      soilType: '',
      startMonth: '',
      endMonth: '',
      manager: '',
      contact: ''
    });
  };

  return (
    <div>
      <h2>Crops</h2>
      {message && <p style={{ color: message.includes('success') ? 'green' : 'red' }}>{message}</p>}
      
      {(user?.role === 'FARMER' || user?.role === 'USER') && (
        <button onClick={() => setShowForm(true)} style={{ marginBottom: '20px' }}>
          {editingCrop ? 'Edit Crop' : 'Add New Crop'}
        </button>
      )}

      {showForm && (
        <div style={{ border: '1px solid #ccc', padding: '20px', marginBottom: '20px' }}>
          <h3>{editingCrop ? 'Edit Crop' : 'Add New Crop'}</h3>
          <form onSubmit={handleSubmit}>
            <label>Crop Name</label>
            <input name="cropName" value={form.cropName} onChange={handleChange} required /><br/>
            <label>Acres</label>
            <input name="acres" value={form.acres} onChange={handleChange} required /><br/>
            <label>Location</label>
            <input name="location" value={form.location} onChange={handleChange} required /><br/>
            <label>Soil Type</label>
            <input name="soilType" value={form.soilType} onChange={handleChange} /><br/>
            <label>Start Month</label>
            <input name="startMonth" value={form.startMonth} onChange={handleChange} /><br/>
            <label>End Month</label>
            <input name="endMonth" value={form.endMonth} onChange={handleChange} /><br/>
            <label>Manager</label>
            <input name="manager" value={form.manager} onChange={handleChange} /><br/>
            <label>Contact</label>
            <input name="contact" value={form.contact} onChange={handleChange} /><br/>
            <button type="submit" disabled={loading}>
              {loading ? 'Saving...' : (editingCrop ? 'Update' : 'Add')}
            </button>
            <button type="button" onClick={handleCancel} style={{ marginLeft: '10px' }}>
              Cancel
            </button>
          </form>
        </div>
      )}

      {loading && <p>Loading...</p>}
      
      {!loading && crops.length === 0 ? (
        <p>No crops found.</p>
      ) : (
        <ul>
          {crops.map(crop => (
            <li key={crop.id} style={{ marginBottom: '15px', border: '1px solid #eee', padding: '10px' }}>
              <strong>{crop.cropName}</strong> - {crop.location} ({crop.acres} acres)<br/>
              Soil: {crop.soilType} | {crop.startMonth} to {crop.endMonth}<br/>
              Manager: {crop.manager} | Contact: {crop.contact}<br/>
              {(user?.role === 'FARMER' || user?.role === 'USER') && (
                <>
                  <button onClick={() => handleEdit(crop)} style={{ marginRight: '10px' }}>Edit</button>
                  <button onClick={() => handleDelete(crop.id)}>Delete</button>
                </>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
