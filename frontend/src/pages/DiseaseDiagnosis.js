import { useState, useEffect } from 'react';
import axios from 'axios';
import Button from '../components/ui/Button';
import Alert from '../components/ui/Alert';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function DiseaseDiagnosis() {
  const [cropName, setCropName] = useState('Soybean');
  const [symptoms, setSymptoms] = useState('');
  const [imageFile, setImageFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [history, setHistory] = useState([]);
  const [alert, setAlert] = useState(null);

  const fetchHistory = async () => {
    const token = localStorage.getItem('token');
    try {
      const res = await axios.get(`${API_BASE}/v1/diagnosis/history`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.data?.data) {
        setHistory(res.data.data);
      }
    } catch (e) {
      console.warn('Could not load diagnosis history', e);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImageFile(file);
      setPreviewUrl(URL.createObjectURL(file));
    }
  };

  const handleAnalyze = async (e) => {
    e.preventDefault();
    if (!symptoms.trim()) {
      setAlert({ type: 'error', message: 'Please describe the observed symptoms' });
      return;
    }

    setLoading(true);
    setAlert(null);
    setResult(null);

    const formData = new FormData();
    formData.append('cropName', cropName);
    formData.append('symptoms', symptoms);
    if (imageFile) {
      formData.append('image', imageFile);
    }

    const token = localStorage.getItem('token');

    try {
      const res = await axios.post(`${API_BASE}/v1/diagnosis/analyze`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          Authorization: `Bearer ${token}`
        }
      });
      if (res.data?.data) {
        setResult(res.data.data);
        setAlert({ type: 'success', message: 'Diagnostic evaluation completed successfully!' });
        fetchHistory();
      }
    } catch (err) {
      setAlert({ type: 'error', message: err.response?.data?.message || 'Diagnosis failed. Check inputs.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900">Crop Disease & AI Diagnosis</h1>
        <p className="text-gray-600 mt-1">
          Upload leaf/crop imagery or describe symptoms to receive pathological analysis, confidence scores, and safety-verified treatment protocols.
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

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Input Form */}
        <div className="bg-white rounded-2xl shadow-md border border-gray-100 p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center gap-2">
            <span className="p-2 bg-emerald-100 text-emerald-700 rounded-lg">🔬</span>
            Symptom Scanner
          </h2>

          <form onSubmit={handleAnalyze} className="space-y-4">
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Crop Variety</label>
              <input
                type="text"
                value={cropName}
                onChange={(e) => setCropName(e.target.value)}
                placeholder="e.g. Cotton, Wheat, Tomato, Soybean"
                className="w-full px-3 py-2 border rounded-xl focus:ring-emerald-500 focus:border-emerald-500"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Observed Foliar Symptoms</label>
              <textarea
                rows="4"
                value={symptoms}
                onChange={(e) => setSymptoms(e.target.value)}
                placeholder="Describe leaf spots, curling, discoloration, wilt, lesions, or powdery dust..."
                className="w-full px-3 py-2 border rounded-xl focus:ring-emerald-500 focus:border-emerald-500"
                required
              />
            </div>

            {/* Quick symptom pills */}
            <div className="flex flex-wrap gap-2 text-xs">
              <span className="text-gray-500 self-center">Quick tags:</span>
              <button
                type="button"
                onClick={() => setSymptoms('Leaves turning bright yellow along veins with severe leaf curling and stunted pods')}
                className="px-2 py-1 bg-gray-100 hover:bg-gray-200 rounded-md text-gray-700"
              >
                Yellow Veins & Curling
              </button>
              <button
                type="button"
                onClick={() => setSymptoms('White powdery talc-like coating covering upper surface of leaves')}
                className="px-2 py-1 bg-gray-100 hover:bg-gray-200 rounded-md text-gray-700"
              >
                Powdery Mildew
              </button>
              <button
                type="button"
                onClick={() => setSymptoms('Concentric brown target-board spots and necrotic margins on lower leaves')}
                className="px-2 py-1 bg-gray-100 hover:bg-gray-200 rounded-md text-gray-700"
              >
                Blight / Leaf Spots
              </button>
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Upload Photo (Optional)</label>
              <input
                type="file"
                accept="image/*"
                onChange={handleImageChange}
                className="w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-sm file:font-semibold file:bg-emerald-50 file:text-emerald-700 hover:file:bg-emerald-100"
              />
              {previewUrl && (
                <div className="mt-3">
                  <img src={previewUrl} alt="Preview" className="h-32 rounded-lg object-cover border" />
                </div>
              )}
            </div>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              loading={loading}
              className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-semibold py-3 rounded-xl mt-4"
            >
              {loading ? 'Evaluating Pathology...' : 'Run Disease Diagnosis'}
            </Button>
          </form>
        </div>

        {/* Diagnosis Results Card */}
        <div className="space-y-6">
          {result ? (
            <div className="bg-white rounded-2xl shadow-lg border border-emerald-200 p-6">
              <div className="flex items-start justify-between border-b pb-4">
                <div>
                  <span className={`px-2.5 py-1 rounded-full text-xs font-bold uppercase ${
                    result.severity === 'CRITICAL' ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700'
                  }`}>
                    {result.severity} SEVERITY
                  </span>
                  <h3 className="text-2xl font-extrabold text-gray-900 mt-2">{result.diagnosedDisease}</h3>
                  <p className="text-sm text-gray-500">Pathogen Classification: {result.pathogenCategory}</p>
                </div>
                <div className="text-right">
                  <div className="text-3xl font-black text-emerald-600">
                    {(result.confidenceScore * 100).toFixed(0)}%
                  </div>
                  <div className="text-xs text-gray-500">AI Confidence</div>
                </div>
              </div>

              <div className="mt-6 space-y-4">
                <div className="bg-emerald-50 rounded-xl p-4 border border-emerald-100">
                  <h4 className="font-bold text-emerald-900 text-sm mb-1">🧪 Recommended Chemical Protocol</h4>
                  <p className="text-sm text-emerald-800">{result.chemicalTreatment}</p>
                </div>

                <div className="bg-green-50 rounded-xl p-4 border border-green-100">
                  <h4 className="font-bold text-green-900 text-sm mb-1">🌿 Organic & Bio-Remedy</h4>
                  <p className="text-sm text-green-800">{result.organicRemedy}</p>
                </div>

                {result.expertReviewRequired && (
                  <div className="bg-amber-50 rounded-xl p-4 border border-amber-200 text-xs text-amber-800">
                    ⚠️ <strong>Expert Attention Advised:</strong> Confidence falls within review threshold or pathogen is high-virulence. We recommend scheduling an agronomy consultation.
                  </div>
                )}
              </div>
            </div>
          ) : (
            <div className="bg-gray-50 rounded-2xl border-2 border-dashed border-gray-200 p-12 text-center text-gray-500">
              <span className="text-4xl block mb-2">🌿</span>
              <p className="font-medium">No diagnosis run yet</p>
              <p className="text-xs mt-1">Fill in crop symptoms on the left to inspect disease analysis and dosage protocols.</p>
            </div>
          )}

          {/* Recent Diagnoses */}
          {history.length > 0 && (
            <div className="bg-white rounded-2xl shadow-md border border-gray-100 p-6">
              <h3 className="text-lg font-bold text-gray-900 mb-3">Previous Diagnoses</h3>
              <div className="divide-y divide-gray-100">
                {history.slice(0, 4).map((h) => (
                  <div key={h.id} className="py-3 flex justify-between items-center text-sm">
                    <div>
                      <div className="font-semibold text-gray-900">{h.cropName}: {h.diagnosedDisease}</div>
                      <div className="text-xs text-gray-500">{new Date(h.diagnosedAt).toLocaleDateString()}</div>
                    </div>
                    <span className="text-xs font-bold text-emerald-600">{(h.confidenceScore * 100).toFixed(0)}%</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
