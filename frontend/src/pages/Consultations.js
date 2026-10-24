import { useState, useEffect } from 'react';
import axios from 'axios';
import Button from '../components/ui/Button';
import Alert from '../components/ui/Alert';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function Consultations() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState(null);

  const [bookingForm, setBookingForm] = useState({
    expertId: 2,
    expertName: 'Dr. Anjali Deshmukh',
    expertType: 'VETERINARIAN',
    issueSummary: ''
  });

  const fetchBookings = async () => {
    setLoading(true);
    const token = localStorage.getItem('token');
    try {
      const res = await axios.get(`${API_BASE}/v1/consultations/my-bookings`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.data?.data) {
        setBookings(res.data.data);
      }
    } catch (e) {
      console.warn('Error fetching bookings', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleBook = async (e) => {
    e.preventDefault();
    if (!bookingForm.issueSummary.trim()) {
      setAlert({ type: 'error', message: 'Please describe the reason for consultation.' });
      return;
    }

    const token = localStorage.getItem('token');
    try {
      await axios.post(
        `${API_BASE}/v1/consultations/book`,
        bookingForm,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setAlert({ type: 'success', message: 'Consultation appointment scheduled!' });
      setBookingForm({ expertId: 2, expertName: 'Dr. Anjali Deshmukh', expertType: 'VETERINARIAN', issueSummary: '' });
      fetchBookings();
    } catch (err) {
      setAlert({ type: 'error', message: 'Booking failed.' });
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900">Expert Tele-Consultations</h1>
        <p className="text-gray-600 mt-1">
          Connect with verified veterinary officers, animal surgeons, and agronomists via video consultation and diagnostic report sharing.
        </p>
      </div>

      {alert && (
        <Alert
          type={alert.type}
          message={alert.message}
          onClose={() => setAlert(null)}
          className="mb-6"
        />
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Booking Form */}
        <div className="bg-white rounded-2xl shadow-md border border-gray-100 p-6 space-y-4">
          <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
            <span>📅</span> Schedule New Session
          </h2>

          <form onSubmit={handleBook} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">Select Verified Expert</label>
              <select
                value={bookingForm.expertName}
                onChange={(e) => {
                  const name = e.target.value;
                  const type = name.includes('Agronomist') ? 'AGRONOMIST' : 'VETERINARIAN';
                  setBookingForm({ ...bookingForm, expertName: name, expertType: type });
                }}
                className="w-full px-3 py-2 border rounded-xl text-sm"
              >
                <option value="Dr. Anjali Deshmukh (MVSc Surgery)">Dr. Anjali Deshmukh (MVSc Surgery - Vet)</option>
                <option value="Dr. Rajesh Thorat (BVSc & AH)">Dr. Rajesh Thorat (BVSc & AH - Cattle Specialist)</option>
                <option value="Prof. S. R. Joshi (Agronomist)">Prof. S. R. Joshi (Agronomist - Crop Pathology)</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">Clinical Issue or Query</label>
              <textarea
                rows="4"
                placeholder="Describe animal symptoms, milk yield changes, or crop condition in detail..."
                value={bookingForm.issueSummary}
                onChange={(e) => setBookingForm({ ...bookingForm, issueSummary: e.target.value })}
                className="w-full px-3 py-2 border rounded-xl text-sm focus:ring-emerald-500"
                required
              />
            </div>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              loading={loading}
              className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-semibold py-3 rounded-xl shadow-md"
            >
              {loading ? 'Booking Session...' : 'Book Video Consultation'}
            </Button>
          </form>
        </div>

        {/* Bookings List */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-white rounded-2xl shadow-md border border-gray-100 p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
              <span>🩺</span> Consultation Sessions ({bookings.length})
            </h2>

            {bookings.length > 0 ? (
              <div className="space-y-4">
                {bookings.map((b) => (
                  <div key={b.id} className="p-5 border rounded-2xl bg-gray-50/50 space-y-2">
                    <div className="flex justify-between items-start">
                      <div>
                        <span className={`px-2.5 py-0.5 rounded-full text-xs font-bold uppercase ${
                          b.status === 'CONFIRMED' ? 'bg-blue-100 text-blue-800' : 'bg-amber-100 text-amber-800'
                        }`}>
                          {b.status}
                        </span>
                        <h3 className="text-lg font-extrabold text-gray-900 mt-1">{b.expertName}</h3>
                        <p className="text-xs text-gray-500">{b.expertType}</p>
                      </div>
                      <div className="text-right">
                        <span className="text-xs font-semibold text-gray-600">
                          {new Date(b.scheduledSlot).toLocaleString()}
                        </span>
                      </div>
                    </div>

                    <p className="text-sm text-gray-700 italic border-l-2 border-gray-300 pl-3 my-2">
                      "{b.issueSummary}"
                    </p>

                    <div className="flex items-center justify-between pt-2 border-t text-xs">
                      <span className="text-gray-500 font-mono">Room: {b.meetingRoomId}</span>
                      <button
                        type="button"
                        onClick={() => window.open(`https://meet.jit.si/${b.meetingRoomId}`, '_blank')}
                        className="px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg shadow-sm"
                      >
                        Join Video Call &rarr;
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="py-12 text-center text-gray-400">
                No active consultations scheduled. Book a session with an expert using the form on the left.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
