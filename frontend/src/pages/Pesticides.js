import { useEffect, useState } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Select from '../components/ui/Select';
import Card from '../components/ui/Card';
import Alert from '../components/ui/Alert';
import Modal from '../components/ui/Modal';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

const statusOptions = [
  { value: 'PENDING', label: 'Pending' },
  { value: 'APPROVED', label: 'Approved' },
  { value: 'REJECTED', label: 'Rejected' }
];

export default function Pesticides() {
  const { user } = useAuth();
  const [pesticides, setPesticides] = useState([]);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
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
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPesticides();
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
        setMessageType('success');
      } else {
        await axios.post(`${API_BASE}/pesticides`, pesticideData);
        setMessage('Pesticide added successfully');
        setMessageType('success');
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
      setMessageType('error');
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
      setMessageType('success');
      fetchPesticides();
    } catch (error) {
      setMessage('Failed to delete pesticide');
      setMessageType('error');
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
      setMessageType('success');
      fetchPesticides();
    } catch (error) {
      setMessage('Failed to update status');
      setMessageType('error');
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

  const getStatusColor = (status) => {
    switch (status) {
      case 'APPROVED':
        return 'bg-green-100 text-green-800';
      case 'REJECTED':
        return 'bg-red-100 text-red-800';
      case 'PENDING':
      default:
        return 'bg-yellow-100 text-yellow-800';
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-secondary-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Pesticide Management</h1>
            <p className="text-gray-600 mt-1">Track and manage pesticide applications and approvals</p>
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
              Add New Pesticide
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
        {loading && pesticides.length === 0 ? (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
          </div>
        ) : (
          <>
            {/* Empty State */}
            {!loading && pesticides.length === 0 ? (
              <Card className="text-center py-12">
                <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
                </svg>
                <h3 className="mt-2 text-sm font-medium text-gray-900">No pesticide records found</h3>
                <p className="mt-1 text-sm text-gray-500">Get started by adding your first pesticide record.</p>
                {(user?.role === 'FARMER' || user?.role === 'USER') && (
                  <div className="mt-6">
                    <Button variant="primary" onClick={() => setShowForm(true)}>
                      Add Pesticide
                    </Button>
                  </div>
                )}
              </Card>
            ) : (
              /* Pesticides Grid */
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {pesticides.map(pesticide => (
                  <Card key={pesticide.id} className="hover:shadow-lg transition-shadow">
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex items-center">
                        <div className="bg-blue-100 rounded-full p-3 mr-3">
                          <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
                          </svg>
                        </div>
                        <div>
                          <h3 className="text-lg font-semibold text-gray-900">{pesticide.pesticideName}</h3>
                          <p className="text-sm text-gray-500">{pesticide.cropName}</p>
                        </div>
                      </div>
                      <span className={`text-xs font-medium px-2.5 py-0.5 rounded ${getStatusColor(pesticide.pesticideStatus)}`}>
                        {pesticide.pesticideStatus}
                      </span>
                    </div>

                    <div className="space-y-2 text-sm text-gray-600">
                      <div className="flex items-center">
                        <svg className="w-4 h-4 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        Quantity: {pesticide.pesticideQuantity}
                      </div>
                      <div className="flex items-center">
                        <svg className="w-4 h-4 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4" />
                        </svg>
                        Acres: {pesticide.acres}
                      </div>
                      <div className="flex items-center">
                        <svg className="w-4 h-4 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                        Disease: {pesticide.diseaseName || 'N/A'}
                      </div>
                    </div>

                    {(user?.role === 'FARMER' || user?.role === 'USER') && (
                      <div className="mt-4 pt-4 border-t border-gray-100">
                        <div className="flex gap-2 mb-3">
                          <Button 
                            variant="outline" 
                            size="sm" 
                            onClick={() => handleEdit(pesticide)}
                            className="flex-1"
                          >
                            Edit
                          </Button>
                          <Button 
                            variant="danger" 
                            size="sm" 
                            onClick={() => handleDelete(pesticide.id)}
                            className="flex-1"
                          >
                            Delete
                          </Button>
                        </div>
                        <Select
                          name="status"
                          value={pesticide.pesticideStatus}
                          onChange={(e) => handleStatusUpdate(pesticide.id, e.target.value)}
                          options={statusOptions}
                          className="text-sm"
                        />
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
          title={editingPesticide ? 'Edit Pesticide' : 'Add New Pesticide'}
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
                placeholder="e.g., Wheat"
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
                label="Disease Name"
                name="diseaseName"
                value={form.diseaseName}
                onChange={handleChange}
                placeholder="e.g., Rust"
              />
              <Input
                label="Pesticide Name"
                name="pesticideName"
                value={form.pesticideName}
                onChange={handleChange}
                required
                placeholder="e.g., Fungicide X"
              />
              <Input
                label="Pesticide Quantity"
                name="pesticideQuantity"
                value={form.pesticideQuantity}
                onChange={handleChange}
                required
                placeholder="e.g., 500ml"
              />
              <Select
                label="Status"
                name="pesticideStatus"
                value={form.pesticideStatus}
                onChange={handleChange}
                options={statusOptions}
                required
              />
            </div>

            <div className="flex gap-3 mt-6">
              <Button
                type="submit"
                variant="primary"
                loading={loading}
                className="flex-1"
              >
                {loading ? 'Saving...' : (editingPesticide ? 'Update Pesticide' : 'Add Pesticide')}
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
