import { useState, useEffect } from 'react';
import axios from 'axios';
import Button from '../components/ui/Button';
import Alert from '../components/ui/Alert';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function MandiAndFinance() {
  const [commodity, setCommodity] = useState('');
  const [mandiPrices, setMandiPrices] = useState([]);
  const [analysis, setAnalysis] = useState(null);
  const [alert, setAlert] = useState(null);

  const [newTx, setNewTx] = useState({
    transactionType: 'EXPENSE',
    category: 'SEEDS',
    amount: '',
    paymentMode: 'UPI',
    notes: ''
  });

  const fetchData = async () => {
    const token = localStorage.getItem('token');
    const headers = { Authorization: `Bearer ${token}` };

    try {
      const [mandiRes, analysisRes] = await Promise.all([
        axios.get(`${API_BASE}/v1/finance/mandi-prices${commodity ? `?commodity=${commodity}` : ''}`, { headers }),
        axios.get(`${API_BASE}/v1/finance/profit-analysis`, { headers })
      ]);

      if (mandiRes.data?.data) setMandiPrices(mandiRes.data.data);
      if (analysisRes.data?.data) setAnalysis(analysisRes.data.data);
    } catch (e) {
      console.warn('Error loading financial data', e);
    }
  };

  useEffect(() => {
    fetchData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [commodity]);

  const handleRecordTx = async (e) => {
    e.preventDefault();
    if (!newTx.amount || Number(newTx.amount) <= 0) {
      setAlert({ type: 'error', message: 'Enter a valid transaction amount.' });
      return;
    }

    const token = localStorage.getItem('token');
    try {
      await axios.post(
        `${API_BASE}/v1/finance/transactions`,
        {
          ...newTx,
          amount: Number(newTx.amount)
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setAlert({ type: 'success', message: 'Financial transaction recorded in ledger!' });
      setNewTx({ transactionType: 'EXPENSE', category: 'SEEDS', amount: '', paymentMode: 'UPI', notes: '' });
      fetchData();
    } catch (err) {
      setAlert({ type: 'error', message: 'Failed to record transaction.' });
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900">Mandi Market Prices & Farm Finances</h1>
        <p className="text-gray-600 mt-1">
          Agmarketnet commodity benchmark rates, agricultural input cost tracking, and profitability analytics.
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

      {/* Financial Health Scorecard */}
      {analysis && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          <div className="bg-white rounded-2xl p-5 border shadow-sm">
            <span className="text-xs font-bold text-gray-500 uppercase">Gross Revenue</span>
            <div className="text-2xl font-black text-emerald-600 mt-1">
              ₹{analysis.totalRevenue?.toLocaleString()}
            </div>
          </div>
          <div className="bg-white rounded-2xl p-5 border shadow-sm">
            <span className="text-xs font-bold text-gray-500 uppercase">Total Expenses</span>
            <div className="text-2xl font-black text-rose-600 mt-1">
              ₹{analysis.totalExpenses?.toLocaleString()}
            </div>
          </div>
          <div className="bg-white rounded-2xl p-5 border shadow-sm">
            <span className="text-xs font-bold text-gray-500 uppercase">Net Farm Profit</span>
            <div className="text-2xl font-black text-gray-900 mt-1">
              ₹{analysis.netProfit?.toLocaleString()}
            </div>
          </div>
          <div className="bg-white rounded-2xl p-5 border shadow-sm">
            <span className="text-xs font-bold text-gray-500 uppercase">Return on Investment (ROI)</span>
            <div className="text-2xl font-black text-blue-600 mt-1">
              {analysis.returnOnInvestmentPercent?.toFixed(1)}%
            </div>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        {/* Mandi Rates */}
        <div className="bg-white rounded-2xl shadow-md border border-gray-100 p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
              <span className="p-2 bg-amber-100 text-amber-700 rounded-lg">🌾</span>
              Agmarketnet Benchmark Rates
            </h2>
            <select
              value={commodity}
              onChange={(e) => setCommodity(e.target.value)}
              className="px-3 py-1.5 border rounded-lg text-sm focus:ring-emerald-500"
            >
              <option value="">All Commodities</option>
              <option value="Soybean">Soybean</option>
              <option value="Wheat">Wheat</option>
              <option value="Cotton">Cotton</option>
              <option value="Paddy (Basmati)">Paddy (Basmati)</option>
              <option value="Tomato">Tomato</option>
              <option value="Onion">Onion</option>
            </select>
          </div>

          <div className="divide-y divide-gray-100">
            {mandiPrices.map((m, idx) => (
              <div key={idx} className="py-3.5 flex justify-between items-center">
                <div>
                  <div className="font-bold text-gray-900">{m.commodity}</div>
                  <div className="text-xs text-gray-500">{m.marketName}, {m.state}</div>
                </div>
                <div className="text-right">
                  <div className="text-base font-extrabold text-emerald-700">₹{m.modalPricePerQuintal}/Q</div>
                  <div className="text-xs text-gray-400">Range: ₹{m.minPricePerQuintal} - ₹{m.maxPricePerQuintal}</div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Ledger Entry Form */}
        <div className="bg-white rounded-2xl shadow-md border border-gray-100 p-6">
          <h2 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
            <span className="p-2 bg-emerald-100 text-emerald-700 rounded-lg">📝</span>
            Record Income or Input Cost
          </h2>

          <form onSubmit={handleRecordTx} className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Type</label>
                <select
                  value={newTx.transactionType}
                  onChange={(e) => setNewTx({ ...newTx, transactionType: e.target.value })}
                  className="w-full px-3 py-2 border rounded-xl text-sm"
                >
                  <option value="EXPENSE">Expense (Input Cost)</option>
                  <option value="REVENUE">Revenue (Crop Sale)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Category</label>
                <select
                  value={newTx.category}
                  onChange={(e) => setNewTx({ ...newTx, category: e.target.value })}
                  className="w-full px-3 py-2 border rounded-xl text-sm"
                >
                  {newTx.transactionType === 'EXPENSE' ? (
                    <>
                      <option value="SEEDS">Seeds</option>
                      <option value="FERTILIZERS">Fertilizers</option>
                      <option value="PESTICIDES">Pesticides & Bio-sprays</option>
                      <option value="IRRIGATION">Water / Electricity</option>
                      <option value="LABOR">Farm Labor</option>
                      <option value="MACHINERY">Tractor & Equipment</option>
                    </>
                  ) : (
                    <>
                      <option value="CROP_SALE">Crop Harvest Sale</option>
                      <option value="BYPRODUCT_SALE">Byproduct / Fodder Sale</option>
                    </>
                  )}
                </select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Amount (₹)</label>
                <input
                  type="number"
                  placeholder="e.g. 12500"
                  value={newTx.amount}
                  onChange={(e) => setNewTx({ ...newTx, amount: e.target.value })}
                  className="w-full px-3 py-2 border rounded-xl text-sm"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Payment Mode</label>
                <select
                  value={newTx.paymentMode}
                  onChange={(e) => setNewTx({ ...newTx, paymentMode: e.target.value })}
                  className="w-full px-3 py-2 border rounded-xl text-sm"
                >
                  <option value="UPI">UPI / GooglePay</option>
                  <option value="CASH">Cash</option>
                  <option value="BANK_TRANSFER">Direct Bank Transfer</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 mb-1">Notes / Invoice No</label>
              <input
                type="text"
                placeholder="e.g. 5 bags of DAP purchased from Agro-dealer"
                value={newTx.notes}
                onChange={(e) => setNewTx({ ...newTx, notes: e.target.value })}
                className="w-full px-3 py-2 border rounded-xl text-sm"
              />
            </div>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-semibold py-2.5 rounded-xl"
            >
              Add to Farm Ledger
            </Button>
          </form>
        </div>
      </div>
    </div>
  );
}
