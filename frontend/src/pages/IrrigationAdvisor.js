import { useState, useEffect } from 'react';
import axios from 'axios';
import Button from '../components/ui/Button';
import Alert from '../components/ui/Alert';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function IrrigationAdvisor() {
  const [plots, setPlots] = useState([]);
  const [selectedPlot, setSelectedPlot] = useState('1');
  const [telemetry, setTelemetry] = useState({ moisture: 32.5, temp: 30.0 });
  const [recommendation, setRecommendation] = useState(null);
  const [loading, setLoading] = useState(false);
  const [alert, setAlert] = useState(null);

  const fetchPlots = async () => {
    const token = localStorage.getItem('token');
    try {
      const res = await axios.get(`${API_BASE}/v1/farm/plots`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.data?.data && res.data.data.length > 0) {
        setPlots(res.data.data);
        setSelectedPlot(String(res.data.data[0].id));
      }
    } catch (e) {
      console.warn('Could not load plots', e);
    }
  };

  useEffect(() => {
    fetchPlots();
  }, []);

  const handleSimulateTelemetry = async () => {
    const token = localStorage.getItem('token');
    try {
      await axios.post(
        `${API_BASE}/v1/irrigation/telemetry`,
        {
          plotId: Number(selectedPlot),
          moisturePercentage: Number(telemetry.moisture),
          soilTemperature: Number(telemetry.temp),
          sensorId: 'IOT_SOIL_UI_01'
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setAlert({ type: 'success', message: 'IoT Soil moisture telemetry ingested successfully!' });
    } catch (e) {
      setAlert({ type: 'error', message: 'Failed to record sensor telemetry.' });
    }
  };

  const handleCalculateIrrigation = async () => {
    setLoading(true);
    setAlert(null);
    const token = localStorage.getItem('token');
    try {
      const res = await axios.post(
        `${API_BASE}/v1/irrigation/calculate?plotId=${selectedPlot}`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      if (res.data?.data) {
        setRecommendation(res.data.data);
        setAlert({ type: 'success', message: 'Watering schedule calculated!' });
      }
    } catch (e) {
      setAlert({ type: 'error', message: 'Could not calculate irrigation need. Ensure plot exists.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900">Smart Irrigation Advisor</h1>
        <p className="text-gray-600 mt-1">
          Weather-informed evapotranspiration scheduling integrated with real-time IoT soil moisture telemetry.
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
        {/* Plot and Sensor Inputs */}
        <div className="bg-white rounded-2xl shadow-md border border-gray-100 p-6 space-y-6">
          <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
            <span className="p-2 bg-blue-100 text-blue-700 rounded-lg">💧</span>
            Plot & Telemetry Config
          </h2>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">Target Farm Plot</label>
            <select
              value={selectedPlot}
              onChange={(e) => setSelectedPlot(e.target.value)}
              className="w-full px-3 py-2 border rounded-xl focus:ring-blue-500 focus:border-blue-500 text-sm"
            >
              {plots.length > 0 ? (
                plots.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.plotName} ({p.currentCrop || 'Crop'} - {p.acres} acres)
                  </option>
                ))
              ) : (
                <option value="1">North Orchard Plot A (4.5 acres)</option>
              )}
            </select>
          </div>

          <div className="border-t pt-4">
            <h3 className="text-sm font-bold text-gray-800 mb-2">Simulate IoT Sensor Ingestion</h3>
            <div className="grid grid-cols-2 gap-3 mb-3">
              <div>
                <label className="block text-xs text-gray-600 mb-1">Moisture (%)</label>
                <input
                  type="number"
                  value={telemetry.moisture}
                  onChange={(e) => setTelemetry({ ...telemetry, moisture: e.target.value })}
                  className="w-full px-2 py-1.5 border rounded-lg text-sm"
                  min="0"
                  max="100"
                />
              </div>
              <div>
                <label className="block text-xs text-gray-600 mb-1">Soil Temp (°C)</label>
                <input
                  type="number"
                  value={telemetry.temp}
                  onChange={(e) => setTelemetry({ ...telemetry, temp: e.target.value })}
                  className="w-full px-2 py-1.5 border rounded-lg text-sm"
                />
              </div>
            </div>
            <button
              type="button"
              onClick={handleSimulateTelemetry}
              className="w-full py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-xs font-semibold rounded-lg"
            >
              Push Sensor Reading
            </button>
          </div>

          <Button
            type="button"
            variant="primary"
            size="lg"
            loading={loading}
            onClick={handleCalculateIrrigation}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 rounded-xl shadow-md"
          >
            {loading ? 'Evaluating ET0...' : 'Calculate Irrigation Need'}
          </Button>
        </div>

        {/* Results Card */}
        <div className="lg:col-span-2 space-y-6">
          {recommendation ? (
            <div className="bg-white rounded-2xl shadow-lg border border-blue-200 p-6">
              <div className="flex justify-between items-start border-b pb-4">
                <div>
                  <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-xs font-bold uppercase">
                    Status: {recommendation.status}
                  </span>
                  <h3 className="text-2xl font-black text-gray-900 mt-2">
                    {recommendation.waterVolumeLiters > 0
                      ? `${recommendation.waterVolumeLiters.toLocaleString()} Liters Required`
                      : 'Irrigation Suspended'}
                  </h3>
                  <p className="text-sm text-gray-500">Date: {recommendation.scheduledDate}</p>
                </div>
                <div className="text-right">
                  <div className="text-2xl font-black text-blue-600">
                    {recommendation.currentSoilMoisturePercent}%
                  </div>
                  <div className="text-xs text-gray-500">Soil Moisture</div>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4 mt-6">
                <div className="bg-blue-50 rounded-xl p-4 border border-blue-100">
                  <div className="text-xs text-blue-700 font-semibold">Precipitation Forecast</div>
                  <div className="text-lg font-bold text-blue-900">{recommendation.forecastedRainfallMm} mm</div>
                </div>
                <div className="bg-blue-50 rounded-xl p-4 border border-blue-100">
                  <div className="text-xs text-blue-700 font-semibold">Ambient Temperature</div>
                  <div className="text-lg font-bold text-blue-900">{recommendation.temperatureCelsius}°C</div>
                </div>
              </div>

              <div className="mt-4 p-4 bg-emerald-50 rounded-xl border border-emerald-100">
                <h4 className="text-xs font-bold text-emerald-900 uppercase tracking-wider mb-1">Agronomic Reasoning</h4>
                <p className="text-sm text-emerald-800">{recommendation.recommendationReason}</p>
              </div>
            </div>
          ) : (
            <div className="bg-gray-50 rounded-2xl border-2 border-dashed border-gray-200 p-12 text-center text-gray-500">
              <span className="text-4xl block mb-2">🌧️</span>
              <p className="font-semibold">No irrigation calculation requested yet</p>
              <p className="text-xs mt-1">Select your plot and click "Calculate Irrigation Need" to run weather and telemetry modeling.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
