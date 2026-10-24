import { useState, useEffect } from 'react';
import axios from 'axios';
import Button from '../components/ui/Button';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function SchemeFinder() {
  const [profile, setProfile] = useState({
    acres: 3.5,
    state: 'Maharashtra',
    beneficiaryType: 'ALL'
  });
  const [evaluations, setEvaluations] = useState([]);
  const [loading, setLoading] = useState(false);

  const checkEligibility = async () => {
    setLoading(true);
    const token = localStorage.getItem('token');
    try {
      const res = await axios.get(
        `${API_BASE}/v1/schemes/check-eligibility?acres=${profile.acres}&state=${profile.state}&beneficiaryType=${profile.beneficiaryType}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      if (res.data?.data) {
        setEvaluations(res.data.data);
      }
    } catch (e) {
      console.warn('Error fetching scheme eligibility', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkEligibility();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900">Government Agricultural Schemes & Subsidies</h1>
        <p className="text-gray-600 mt-1">
          Automated eligibility evaluator matching your landholding, crop categories, and regional state jurisdiction with Central and State welfare initiatives.
        </p>
      </div>

      {/* Filter bar */}
      <div className="bg-white p-6 rounded-2xl shadow-md border border-gray-100 mb-8">
        <h2 className="text-base font-bold text-gray-900 mb-4 flex items-center gap-2">
          <span>⚙️</span> Your Farmer Profile Parameters
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
          <div>
            <label className="block text-xs font-semibold text-gray-700 mb-1">Landholding Size (Acres)</label>
            <input
              type="number"
              step="0.1"
              value={profile.acres}
              onChange={(e) => setProfile({ ...profile, acres: e.target.value })}
              className="w-full px-3 py-2 border rounded-xl text-sm focus:ring-emerald-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 mb-1">State / UT</label>
            <select
              value={profile.state}
              onChange={(e) => setProfile({ ...profile, state: e.target.value })}
              className="w-full px-3 py-2 border rounded-xl text-sm focus:ring-emerald-500"
            >
              <option value="Maharashtra">Maharashtra</option>
              <option value="Madhya Pradesh">Madhya Pradesh</option>
              <option value="Gujarat">Gujarat</option>
              <option value="Punjab">Punjab</option>
              <option value="Haryana">Haryana</option>
              <option value="Karnataka">Karnataka</option>
              <option value="Uttar Pradesh">Uttar Pradesh</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 mb-1">Beneficiary Category</label>
            <select
              value={profile.beneficiaryType}
              onChange={(e) => setProfile({ ...profile, beneficiaryType: e.target.value })}
              className="w-full px-3 py-2 border rounded-xl text-sm focus:ring-emerald-500"
            >
              <option value="ALL">All Farmers</option>
              <option value="SMALL_AND_MARGINAL">Small & Marginal (&lt; 5 acres)</option>
              <option value="LIVESTOCK_OWNER">Livestock / Dairy Producer</option>
              <option value="WOMEN_FARMER">Women Farmer (Mahila Kisan)</option>
            </select>
          </div>

          <Button
            type="button"
            variant="primary"
            loading={loading}
            onClick={checkEligibility}
            className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-semibold py-2.5 rounded-xl"
          >
            Check Eligibility
          </Button>
        </div>
      </div>

      {/* Scheme Cards */}
      <div className="space-y-6">
        {evaluations.map((item, idx) => {
          const s = item.scheme;
          return (
            <div
              key={idx}
              className={`p-6 rounded-2xl border transition-all ${
                item.isEligible
                  ? 'bg-white border-emerald-300 shadow-md ring-1 ring-emerald-200'
                  : 'bg-gray-50 border-gray-200 opacity-75'
              }`}
            >
              <div className="flex flex-col md:flex-row justify-between md:items-start gap-4">
                <div>
                  <div className="flex items-center gap-2 mb-2">
                    <span
                      className={`px-3 py-0.5 rounded-full text-xs font-extrabold uppercase ${
                        item.isEligible
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-gray-200 text-gray-700'
                      }`}
                    >
                      {item.status}
                    </span>
                    <span className="text-xs text-gray-500">{s.ministry}</span>
                  </div>

                  <h3 className="text-2xl font-black text-gray-900">{s.schemeName}</h3>
                  <p className="text-sm text-gray-600 mt-1 max-w-3xl">{s.description}</p>
                </div>

                <div className="text-right shrink-0">
                  <a
                    href={s.officialPortalUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-block px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs rounded-xl shadow-sm"
                  >
                    Official Portal &rarr;
                  </a>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-6 pt-4 border-t">
                <div className="bg-emerald-50/60 p-4 rounded-xl border border-emerald-100">
                  <h4 className="text-xs font-bold text-emerald-900 uppercase mb-1">Financial Benefit</h4>
                  <p className="text-sm text-emerald-800 font-medium">{s.benefitSummary}</p>
                </div>

                <div className="bg-blue-50/60 p-4 rounded-xl border border-blue-100">
                  <h4 className="text-xs font-bold text-blue-900 uppercase mb-1">How to Apply</h4>
                  <p className="text-xs text-blue-800">{s.applicationGuidance}</p>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
