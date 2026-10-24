import { useState, useEffect } from 'react';
import axios from 'axios';
import Button from '../components/ui/Button';
import Alert from '../components/ui/Alert';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function OutbreakRadar() {
  const [outbreaks, setOutbreaks] = useState([]);
  const [nearbyOutbreaks, setNearbyOutbreaks] = useState([]);
  const [userCoords, setUserCoords] = useState({ lat: 18.5204, lon: 73.8567, radius: 50.0 });
  const [alert, setAlert] = useState(null);

  const [newIncident, setNewIncident] = useState({
    diseaseName: '',
    affectedHost: '',
    district: '',
    state: 'Maharashtra',
    latitude: 18.5204,
    longitude: 73.8567,
    radiusKm: 25.0,
    severity: 'HIGH'
  });

  const fetchOutbreaks = async () => {
    const token = localStorage.getItem('token');
    const headers = { Authorization: `Bearer ${token}` };

    try {
      const [allRes, nearbyRes] = await Promise.all([
        axios.get(`${API_BASE}/v1/radar/outbreaks`, { headers }),
        axios.get(`${API_BASE}/v1/radar/nearby?latitude=${userCoords.lat}&longitude=${userCoords.lon}&radiusKm=${userCoords.radius}`, { headers })
      ]);
      if (allRes.data?.data) setOutbreaks(allRes.data.data);
      if (nearbyRes.data?.data) setNearbyOutbreaks(nearbyRes.data.data);
    } catch (e) {
      console.warn('Error loading radar data', e);
    }
  };

  useEffect(() => {
    fetchOutbreaks();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userCoords.radius]);

  const handleReport = async (e) => {
    e.preventDefault();
    if (!newIncident.diseaseName || !newIncident.affectedHost) {
      setAlert({ type: 'error', message: 'Please specify disease and host.' });
      return;
    }

    const token = localStorage.getItem('token');
    try {
      await axios.post(
        `${API_BASE}/v1/radar/report`,
        newIncident,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setAlert({ type: 'success', message: 'Outbreak reported! Community alert dispatched to nearby producers.' });
      setNewIncident({ diseaseName: '', affectedHost: '', district: '', state: 'Maharashtra', latitude: 18.5204, longitude: 73.8567, radiusKm: 25.0, severity: 'HIGH' });
      fetchOutbreaks();
    } catch (err) {
      setAlert({ type: 'error', message: 'Failed to submit outbreak report.' });
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900">Geospatial Disease Outbreak Radar</h1>
        <p className="text-gray-600 mt-1">
          Real-time epidemiological cluster tracking, early warning radius alerting, and farmer community reports.
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

      {/* Proximity Warning Banner */}
      {nearbyOutbreaks.length > 0 && (
        <div className="mb-8 p-5 bg-rose-50 border-2 border-rose-200 rounded-2xl flex items-start gap-4">
          <span className="text-3xl">🚨</span>
          <div>
            <h3 className="text-base font-extrabold text-rose-900">
              {nearbyOutbreaks.length} Active Epidemic Incident(s) within {userCoords.radius} km
            </h3>
            <p className="text-sm text-rose-800 mt-0.5">
              Pathogens detected near your farm coordinates. Inspect preventative spray protocols immediately on the radar below.
            </p>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Outbreak Map / List */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-2xl shadow-md border border-gray-100 p-6">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                <span className="p-2 bg-rose-100 text-rose-700 rounded-lg">🎯</span>
                Active Disease Clusters
              </h2>
              <div className="flex items-center gap-2">
                <span className="text-xs text-gray-500">Radius:</span>
                <select
                  value={userCoords.radius}
                  onChange={(e) => setUserCoords({ ...userCoords, radius: Number(e.target.value) })}
                  className="px-2.5 py-1 text-xs border rounded-lg focus:ring-emerald-500"
                >
                  <option value={25}>25 km</option>
                  <option value={50}>50 km</option>
                  <option value={100}>100 km</option>
                </select>
              </div>
            </div>

            <div className="space-y-4">
              {outbreaks.map((outbreak) => (
                <div
                  key={outbreak.id}
                  className="p-5 border rounded-2xl hover:border-rose-300 transition-all bg-gradient-to-r from-white to-rose-50/20"
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <span className={`px-2.5 py-0.5 rounded-full text-xs font-bold ${
                        outbreak.severity === 'EPIDEMIC' ? 'bg-rose-100 text-rose-700' : 'bg-amber-100 text-amber-800'
                      }`}>
                        {outbreak.severity} SEVERITY
                      </span>
                      <h3 className="text-lg font-extrabold text-gray-900 mt-1">{outbreak.diseaseName}</h3>
                      <p className="text-sm text-gray-600">Affected Crop / Host: <strong className="text-gray-900">{outbreak.affectedHost}</strong></p>
                    </div>
                    <div className="text-right">
                      <span className="px-2 py-1 bg-gray-100 rounded text-xs font-semibold text-gray-700">
                        {outbreak.radiusKm} km alert radius
                      </span>
                      <div className="text-xs text-gray-400 mt-1">{outbreak.district}, {outbreak.state}</div>
                    </div>
                  </div>
                  <div className="mt-3 text-xs text-gray-500 flex items-center gap-4 border-t pt-2">
                    <span>📅 Reported: {outbreak.reportedDate}</span>
                    <span>👥 Verified Reports: {outbreak.reportCount}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Community Incident Report Form */}
        <div className="bg-white rounded-2xl shadow-md border border-gray-100 p-6">
          <h2 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
            <span className="p-2 bg-amber-100 text-amber-700 rounded-lg">📢</span>
            Report Local Outbreak
          </h2>
          <p className="text-xs text-gray-500 mb-4">
            Help neighboring farmers protect their harvest by reporting unusual pest swarms or viral spreads in your taluka/district.
          </p>

          <form onSubmit={handleReport} className="space-y-3">
            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">Disease / Pest Name</label>
              <input
                type="text"
                placeholder="e.g. Fall Armyworm, Yellow Rust"
                value={newIncident.diseaseName}
                onChange={(e) => setNewIncident({ ...newIncident, diseaseName: e.target.value })}
                className="w-full px-3 py-2 border rounded-xl text-sm"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">Affected Crop / Livestock</label>
              <input
                type="text"
                placeholder="e.g. Maize, Dairy Cattle"
                value={newIncident.affectedHost}
                onChange={(e) => setNewIncident({ ...newIncident, affectedHost: e.target.value })}
                className="w-full px-3 py-2 border rounded-xl text-sm"
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">District</label>
                <input
                  type="text"
                  placeholder="e.g. Pune, Nashik"
                  value={newIncident.district}
                  onChange={(e) => setNewIncident({ ...newIncident, district: e.target.value })}
                  className="w-full px-3 py-2 border rounded-xl text-sm"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Severity</label>
                <select
                  value={newIncident.severity}
                  onChange={(e) => setNewIncident({ ...newIncident, severity: e.target.value })}
                  className="w-full px-3 py-2 border rounded-xl text-sm"
                >
                  <option value="MODERATE">Moderate</option>
                  <option value="HIGH">High Alert</option>
                  <option value="EPIDEMIC">Epidemic</option>
                </select>
              </div>
            </div>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full mt-4 bg-rose-600 hover:bg-rose-700 text-white font-semibold py-2.5 rounded-xl shadow-md"
            >
              Broadcast Outbreak Alert
            </Button>
          </form>
        </div>
      </div>
    </div>
  );
}
