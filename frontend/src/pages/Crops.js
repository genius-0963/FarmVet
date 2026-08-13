import { useEffect, useState } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Card from '../components/ui/Card';
import Alert from '../components/ui/Alert';
import Modal from '../components/ui/Modal';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function Crops() {
  const { user } = useAuth();
  const [crops, setCrops] = useState([]);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
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
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCrops();
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
        setMessageType('success');
      } else {
        await axios.post(`${API_BASE}/crops`, cropData);
        setMessage('Crop added successfully');
        setMessageType('success');
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
      setMessageType('error');
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
      setMessageType('success');
      fetchCrops();
    } catch (error) {
      setMessage('Failed to delete crop');
      setMessageType('error');
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
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-secondary-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Crop Management</h1>
            <p className="text-gray-600 mt-1">Manage your agricultural crops and farming operations</p>
          </div>
          {(user?.role === 'FARMER' || user?.role === 'USER') && (
            <Button 
              variant="primary" 
              onClick={() => setShowForm(true)}
              className="flex items-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m8-8H4" />
              </svg>
              Add New Crop
            </Button>
          )}
        </div>

        {/* Alert */}
        {message && (
          <Alert 
            type={messageType} 
            message={message}
            onClose={() => setMessage('')}
            className="mb-6"
          />
        )}

        {/* Loading State */}
        {loading && crops.length === 0 ? (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
          </div>
        ) : (
          <>
            {/* Empty State */}
            {!loading && crops.length === 0 ? (
              <Card className="text-center py-12">
                <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <h3 className="mt-2 text-sm font-medium text-gray-900">No crops found</h3>
                <p className="mt-1 text-sm text-gray-500">Get started by adding your first crop.</p>
                {(user?.role === 'FARMER' || user?.role === 'USER') && (
                  <div className="mt-6">
                    <Button variant="primary" onClick={() => setShowForm(true)}>
                      Add Crop
                    </Button>
                  </div>
                )}
              </Card>
            ) : (
              /* Crops Grid */
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {crops.map(crop => (
                  <Card key={crop.id} className="hover:shadow-lg transition-shadow">
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex items-center">
                        <div className="bg-green-100 rounded-full p-3 mr-3">
                          <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3.055 11H5a2 2 0 012 2v1a2 2 0 002 2 2 2 0 012 2v2.945M8 3.935V5.5A2.5 2.5 0 0010.5 8h.5a2 2 0 012 2 2 2 0 104 0 2 2 0 012-2h1.064M15 20.488V18a2 2 0 012-2h3.064M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                          </svg>
                        </div>
                        <div>
                          <h3 className="text-lg font-semibold text-gray-900">{crop.cropName}</h3>
                          <p className="text-sm text-gray-500">{crop.location}</p>
                        </div>
                      </div>
                      <span className="bg-green-100 text-green-800 text-xs font-medium px-2.5 py-0.5 rounded">
                        {crop.acres} acres
                      </span>
                    </div>

                    <div className="space-y-2 text-sm text-gray-600">
                      <div className="flex items-center">
                        <svg className="w-4 h-4 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                        </svg>
                        Soil: {crop.soilType || 'N/A'}
                      </div>
                      <div className="flex items-center">
                        <svg className="w-4 h-4 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        {crop.startMonth} - {crop.endMonth}
                      </div>
                      <div className="flex items-center">
                        <svg className="w-4 h-4 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                        </svg>
                        {crop.manager}
                      </div>
                      <div className="flex items-center">
                        <svg className="w-4 h-4 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                        </svg>
                        {crop.contact}
                      </div>
                    </div>

                    {(user?.role === 'FARMER' || user?.role === 'USER') && (
                      <div className="mt-4 pt-4 border-t border-gray-100 flex gap-2">
                        <Button 
                          variant="outline" 
                          size="sm" 
                          onClick={() => handleEdit(crop)}
                          className="flex-1"
                        >
                          Edit
                        </Button>
                        <Button 
                          variant="danger" 
                          size="sm" 
                          onClick={() => handleDelete(crop.id)}
                          className="flex-1"
                        >
                          Delete
                        </Button>
                      </div>
                    )}
                  </Card>
                ))}
              </div>
            )}
          </>
        )}

        {/* Modal Form */}
        <Modal
          isOpen={showForm}
          onClose={handleCancel}
          title={editingCrop ? 'Edit Crop' : 'Add New Crop'}
          size="lg"
        >
          <form onSubmit={handleSubmit}>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                label="Crop Name"
                name="cropName"
                value={form.cropName}
                onChange={handleChange}
                required
                placeholder="e.g., Wheat, Corn"
              />
              <Input
                label="Acres"
                name="acres"
                type="number"
                value={form.acres}
                onChange={handleChange}
                required
                placeholder="e.g., 50"
              />
              <Input
                label="Location"
                name="location"
                value={form.location}
                onChange={handleChange}
                required
                placeholder="e.g., North Field"
              />
              <Input
                label="Soil Type"
                name="soilType"
                value={form.soilType}
                onChange={handleChange}
                placeholder="e.g., Loam, Clay"
              />
              <Input
                label="Start Month"
                name="startMonth"
                value={form.startMonth}
                onChange={handleChange}
                placeholder="e.g., March"
              />
              <Input
                label="End Month"
                name="endMonth"
                value={form.endMonth}
                onChange={handleChange}
                placeholder="e.g., October"
              />
              <Input
                label="Manager"
                name="manager"
                value={form.manager}
                onChange={handleChange}
                placeholder="Farm manager name"
              />
              <Input
                label="Contact"
                name="contact"
                value={form.contact}
                onChange={handleChange}
                placeholder="Contact information"
              />
            </div>

            <div className="flex gap-3 mt-6">
              <Button
                type="submit"
                variant="primary"
                loading={loading}
                className="flex-1"
              >
                {loading ? 'Saving...' : (editingCrop ? 'Update Crop' : 'Add Crop')}
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={handleCancel}
                className="flex-1"
              >
                Cancel
              </Button>
            </div>
          </form>
        </Modal>
      </div>
    </div>
  );
}
