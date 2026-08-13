import { useEffect, useState } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Card from '../components/ui/Card';
import Alert from '../components/ui/Alert';
import Modal from '../components/ui/Modal';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function Veterinarians() {
  const { user } = useAuth();
  const [vets, setVets] = useState([]);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
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
      setMessageType('error');
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
        setMessageType('success');
      } else {
        await axios.post(`${API_BASE}/veterinarians`, form);
        setMessage('Veterinarian added successfully');
        setMessageType('success');
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
      setMessageType('error');
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
      setMessageType('success');
      fetchVets();
    } catch (error) {
      setMessage('Failed to delete veterinarian');
      setMessageType('error');
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
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-secondary-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Veterinarian Services</h1>
            <p className="text-gray-600 mt-1">Connect with veterinary professionals for animal health services</p>
          </div>
          {(user?.role === 'VET' || user?.role === 'USER') && (
            <Button 
              variant="primary" 
              onClick={() => setShowForm(true)}
              className="flex items-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m8-8H4" />
              </svg>
              Add New Veterinarian
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
        {loading && vets.length === 0 ? (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
          </div>
        ) : (
          <>
            {/* Empty State */}
            {!loading && vets.length === 0 ? (
              <Card className="text-center py-12">
                <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
                <h3 className="mt-2 text-sm font-medium text-gray-900">No veterinarians found</h3>
                <p className="mt-1 text-sm text-gray-500">Get started by adding your first veterinarian.</p>
                {(user?.role === 'VET' || user?.role === 'USER') && (
                  <div className="mt-6">
                    <Button variant="primary" onClick={() => setShowForm(true)}>
                      Add Veterinarian
                    </Button>
                  </div>
                )}
              </Card>
            ) : (
              /* Vets Grid */
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {vets.map(vet => (
                  <Card key={vet.id} className="hover:shadow-lg transition-shadow">
                    <div className="flex items-start mb-4">
                      <div className="bg-red-100 rounded-full p-3 mr-3">
                        <svg className="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                        </svg>
                      </div>
                      <div className="flex-1">
                        <h3 className="text-lg font-semibold text-gray-900">{vet.username}</h3>
                        <p className="text-sm text-gray-500">{vet.position}</p>
                      </div>
                    </div>

                    <div className="space-y-2 text-sm text-gray-600 mb-4">
                      <div className="flex items-center">
                        <svg className="w-4 h-4 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                        </svg>
                        {vet.speciality}
                      </div>
                      <div className="flex items-center">
                        <svg className="w-4 h-4 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        {vet.experience} years experience
                      </div>
                      <div className="flex items-center">
                        <svg className="w-4 h-4 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                        </svg>
                        {vet.email}
                      </div>
                      <div className="flex items-center">
                        <svg className="w-4 h-4 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                        </svg>
                        {vet.phone}
                      </div>
                    </div>

                    {vet.about && (
                      <p className="text-sm text-gray-600 mb-4 line-clamp-2">{vet.about}</p>
                    )}

                    {(user?.role === 'VET' || user?.role === 'USER') && (
                      <div className="pt-4 border-t border-gray-100 flex gap-2">
                        <Button 
                          variant="outline" 
                          size="sm" 
                          onClick={() => handleEdit(vet)}
                          className="flex-1"
                        >
                          Edit
                        </Button>
                        <Button 
                          variant="danger" 
                          size="sm" 
                          onClick={() => handleDelete(vet.id)}
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
          title={editingVet ? 'Edit Veterinarian' : 'Add New Veterinarian'}
          size="lg"
        >
          <form onSubmit={handleSubmit}>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                label="Username"
                name="username"
                value={form.username}
                onChange={handleChange}
                required
                placeholder="Dr. John Smith"
              />
              <Input
                label="Position"
                name="position"
                value={form.position}
                onChange={handleChange}
                placeholder="Senior Veterinarian"
              />
              <Input
                label="Speciality"
                name="speciality"
                value={form.speciality}
                onChange={handleChange}
                placeholder="Large Animals"
              />
              <Input
                label="Experience (years)"
                name="experience"
                type="number"
                value={form.experience}
                onChange={handleChange}
                placeholder="10"
              />
              <Input
                label="Email"
                name="email"
                type="email"
                value={form.email}
                onChange={handleChange}
                placeholder="john@example.com"
              />
              <Input
                label="Phone"
                name="phone"
                value={form.phone}
                onChange={handleChange}
                placeholder="+1 234 567 890"
              />
              {!editingVet && (
                <Input
                  label="Password"
                  name="password"
                  type="password"
                  value={form.password}
                  onChange={handleChange}
                  required
                  placeholder="Create password"
                  className="md:col-span-2"
                />
              )}
              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-700 mb-1">About</label>
                <textarea
                  name="about"
                  value={form.about}
                  onChange={handleChange}
                  rows="3"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all duration-200"
                  placeholder="Brief description of qualifications and services..."
                />
              </div>
            </div>

            <div className="flex gap-3 mt-6">
              <Button
                type="submit"
                variant="primary"
                loading={loading}
                className="flex-1"
              >
                {loading ? 'Saving...' : (editingVet ? 'Update Veterinarian' : 'Add Veterinarian')}
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
