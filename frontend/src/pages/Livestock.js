import { useState, useEffect } from 'react';
import axios from 'axios';
import Button from '../components/ui/Button';
import Alert from '../components/ui/Alert';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function Livestock() {
  const [animals, setAnimals] = useState([]);
  const [selectedAnimal, setSelectedAnimal] = useState(null);
  const [vaccinations, setVaccinations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState(null);

  // New animal registration state
  const [newAnimal, setNewAnimal] = useState({
    tagId: '',
    species: 'CATTLE',
    breed: 'Gir',
    gender: 'FEMALE',
    dateOfBirth: '2023-01-01',
    weightKg: 350.0,
    isLactating: true
  });

  // Symptom triage state
  const [clinicalCheck, setClinicalCheck] = useState({ species: 'CATTLE', symptoms: '' });
  const [triageResult, setTriageResult] = useState(null);

  const fetchAnimals = async () => {
    setLoading(true);
    const token = localStorage.getItem('token');
    try {
      const res = await axios.get(`${API_BASE}/v1/livestock`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.data?.data) {
        setAnimals(res.data.data);
        if (res.data.data.length > 0) {
          selectAnimal(res.data.data[0]);
        }
      }
    } catch (e) {
      console.warn('Error fetching livestock', e);
    } finally {
      setLoading(false);
    }
  };

  const selectAnimal = async (animal) => {
    setSelectedAnimal(animal);
    const token = localStorage.getItem('token');
    try {
      const res = await axios.get(`${API_BASE}/v1/livestock/${animal.id}/vaccinations`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.data?.data) {
        setVaccinations(res.data.data);
      }
    } catch (e) {
      console.warn('Error fetching vaccinations', e);
    }
  };

  useEffect(() => {
    fetchAnimals();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleRegisterAnimal = async (e) => {
    e.preventDefault();
    if (!newAnimal.tagId.trim()) {
      setAlert({ type: 'error', message: 'Tag ID is required.' });
      return;
    }

    const token = localStorage.getItem('token');
    try {
      await axios.post(
        `${API_BASE}/v1/livestock`,
        { ...newAnimal, weightKg: Number(newAnimal.weightKg) },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setAlert({ type: 'success', message: 'Animal registered successfully!' });
      setNewAnimal({ tagId: '', species: 'CATTLE', breed: 'Gir', gender: 'FEMALE', dateOfBirth: '2023-01-01', weightKg: 350.0, isLactating: true });
      fetchAnimals();
    } catch (err) {
      setAlert({ type: 'error', message: 'Failed to register animal profile.' });
    }
  };

  const handleTriage = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem('token');
    try {
      const res = await axios.post(
        `${API_BASE}/v1/livestock/diagnose`,
        clinicalCheck,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      if (res.data?.data) {
        setTriageResult(res.data.data);
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Clinical triage evaluation failed.' });
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900">Livestock & Veterinary Health Hub</h1>
        <p className="text-gray-600 mt-1">
          Animal profiles, ear-tag identification, vaccination booster projection, and clinical triage.
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
        {/* Animal Profiles List */}
        <div className="bg-white rounded-2xl shadow-md border border-gray-100 p-6 space-y-4">
          <div className="flex justify-between items-center mb-2">
            <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
              <span>🐄</span> Registered Animals ({animals.length})
            </h2>
          </div>

          <div className="space-y-2 max-h-96 overflow-y-auto">
            {animals.map((a) => (
              <button
                key={a.id}
                type="button"
                onClick={() => selectAnimal(a)}
                className={`w-full text-left p-3.5 rounded-xl border transition-all ${
                  selectedAnimal?.id === a.id
                    ? 'border-emerald-500 bg-emerald-50/50 shadow-sm'
                    : 'border-gray-200 hover:border-gray-300'
                }`}
              >
                <div className="flex justify-between items-center">
                  <span className="font-bold text-gray-900 text-sm">{a.tagId}</span>
                  <span className="px-2 py-0.5 bg-emerald-100 text-emerald-800 rounded-full text-xs font-semibold">
                    {a.species}
                  </span>
                </div>
                <div className="text-xs text-gray-500 mt-1">
                  Breed: {a.breed} &bull; {a.gender} &bull; {a.weightKg} kg
                </div>
              </button>
            ))}
          </div>

          {/* Quick Register */}
          <div className="border-t pt-4">
            <h3 className="text-sm font-bold text-gray-900 mb-3">Register New Animal</h3>
            <form onSubmit={handleRegisterAnimal} className="space-y-2">
              <input
                type="text"
                placeholder="Ear Tag ID (e.g. MH-COW-102)"
                value={newAnimal.tagId}
                onChange={(e) => setNewAnimal({ ...newAnimal, tagId: e.target.value })}
                className="w-full px-3 py-1.5 border rounded-lg text-xs"
                required
              />
              <div className="grid grid-cols-2 gap-2">
                <select
                  value={newAnimal.species}
                  onChange={(e) => setNewAnimal({ ...newAnimal, species: e.target.value })}
                  className="w-full px-2 py-1.5 border rounded-lg text-xs"
                >
                  <option value="CATTLE">Cattle</option>
                  <option value="BUFFALO">Buffalo</option>
                  <option value="GOAT">Goat</option>
                  <option value="SHEEP">Sheep</option>
                  <option value="POULTRY">Poultry</option>
                </select>
                <input
                  type="text"
                  placeholder="Breed (e.g. Sahiwal)"
                  value={newAnimal.breed}
                  onChange={(e) => setNewAnimal({ ...newAnimal, breed: e.target.value })}
                  className="w-full px-2 py-1.5 border rounded-lg text-xs"
                />
              </div>
              <Button type="submit" variant="primary" size="sm" loading={loading} className="w-full bg-emerald-600 text-white mt-1">
                Register Profile
              </Button>
            </form>
          </div>
        </div>

        {/* Selected Animal Vaccination History & Details */}
        <div className="space-y-6">
          {selectedAnimal ? (
            <div className="bg-white rounded-2xl shadow-md border border-gray-100 p-6">
              <div className="flex justify-between items-start border-b pb-4">
                <div>
                  <span className="text-xs font-bold text-gray-400 uppercase">Ear Tag Identification</span>
                  <h3 className="text-2xl font-black text-gray-900">{selectedAnimal.tagId}</h3>
                  <p className="text-sm text-gray-600">
                    {selectedAnimal.breed} {selectedAnimal.species} &bull; {selectedAnimal.healthStatus}
                  </p>
                </div>
                <div className="text-right">
                  <span className="text-xs px-2.5 py-1 bg-green-100 text-green-800 rounded-full font-bold">
                    {selectedAnimal.isLactating ? 'Lactating' : 'Dry Period'}
                  </span>
                </div>
              </div>

              <div className="mt-4">
                <h4 className="text-sm font-bold text-gray-900 mb-3 flex items-center gap-1.5">
                  <span>💉</span> Vaccination Schedule & Projected Boosters
                </h4>
                {vaccinations.length > 0 ? (
                  <div className="space-y-3">
                    {vaccinations.map((v) => (
                      <div key={v.id} className="p-3 bg-gray-50 rounded-xl border text-xs">
                        <div className="font-bold text-gray-900 text-sm">{v.vaccineName}</div>
                        <div className="text-gray-500 mt-1">
                          Administered: <strong>{v.administeredDate}</strong> by {v.administeredBy || 'Vet'}
                        </div>
                        <div className="text-emerald-700 font-semibold mt-1">
                          ⏰ Next Booster Due: <strong>{v.nextDueDate}</strong>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-gray-500 py-3">No vaccinations recorded for this animal yet.</p>
                )}
              </div>
            </div>
          ) : (
            <div className="bg-gray-50 rounded-2xl p-8 text-center text-gray-400">
              Select an animal profile to inspect vaccination history.
            </div>
          )}
        </div>

        {/* Clinical Symptom Triage Box */}
        <div className="bg-white rounded-2xl shadow-md border border-gray-100 p-6 space-y-4">
          <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
            <span>🩺</span> Veterinary Symptom Triage
          </h2>
          <p className="text-xs text-gray-500">
            Rapid clinical assessment for dairy cattle, buffalo, and small ruminants.
          </p>

          <form onSubmit={handleTriage} className="space-y-3">
            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">Species</label>
              <select
                value={clinicalCheck.species}
                onChange={(e) => setClinicalCheck({ ...clinicalCheck, species: e.target.value })}
                className="w-full px-3 py-2 border rounded-xl text-sm"
              >
                <option value="CATTLE">Cattle</option>
                <option value="BUFFALO">Buffalo</option>
                <option value="GOAT">Goat</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">Clinical Signs</label>
              <textarea
                rows="3"
                placeholder="e.g. High fever, swollen udder quarter, blister on tongue..."
                value={clinicalCheck.symptoms}
                onChange={(e) => setClinicalCheck({ ...clinicalCheck, symptoms: e.target.value })}
                className="w-full px-3 py-2 border rounded-xl text-sm"
                required
              />
            </div>

            <Button type="submit" variant="primary" size="md" className="w-full bg-blue-600 text-white">
              Evaluate Symptoms
            </Button>
          </form>

          {triageResult && (
            <div className="mt-4 p-4 rounded-xl border bg-blue-50/50 border-blue-200">
              <span className={`px-2 py-0.5 rounded text-xs font-extrabold uppercase ${
                triageResult.urgency === 'CRITICAL' ? 'bg-red-100 text-red-800' : 'bg-blue-100 text-blue-800'
              }`}>
                {triageResult.urgency} URGENCY
              </span>
              <h4 className="font-extrabold text-gray-900 text-base mt-1">{triageResult.probableDisease}</h4>
              <p className="text-xs text-gray-700 mt-2"><strong>Action:</strong> {triageResult.actionPlan}</p>
              <p className="text-xs text-gray-600 mt-1"><strong>Care:</strong> {triageResult.supportiveCare}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
