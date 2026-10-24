import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Button from '../components/ui/Button';

export default function Home() {
  const { user } = useAuth();

  const services = [
    {
      title: 'Crop & Plot Management',
      description: 'Track farm plots, soil types, crop growth stages, and automated agricultural calendars.',
      icon: '🌱',
      path: '/crops',
      color: 'bg-emerald-100 text-emerald-800'
    },
    {
      title: 'Disease & AI Diagnosis',
      description: 'Upload crop photos or foliar symptoms for automated pathology detection and treatment.',
      icon: '🔬',
      path: '/disease-diagnosis',
      color: 'bg-teal-100 text-teal-800'
    },
    {
      title: 'Pesticide Dosage & PHI',
      description: 'Scientific dosage calculations per acre with Pre-Harvest Interval (PHI) safety countdowns.',
      icon: '🧪',
      path: '/pesticides',
      color: 'bg-indigo-100 text-indigo-800'
    },
    {
      title: 'Smart Irrigation Advisor',
      description: 'Weather-informed evapotranspiration scheduling with IoT soil moisture sensor telemetry.',
      icon: '💧',
      path: '/irrigation',
      color: 'bg-blue-100 text-blue-800'
    },
    {
      title: 'Mandi Rates & Finances',
      description: 'Agmarketnet commodity benchmark prices, expense ledger, and crop profitability analytics.',
      icon: '🌾',
      path: '/finance',
      color: 'bg-amber-100 text-amber-800'
    },
    {
      title: 'Outbreak Radar GIS',
      description: 'Real-time epidemic clusters map with Haversine radius early warnings and community reporting.',
      icon: '🚨',
      path: '/radar',
      color: 'bg-rose-100 text-rose-800'
    },
    {
      title: 'Government Scheme Finder',
      description: 'Profile-based eligibility checker for Central & State subsidies (PM-KISAN, PMFBY, PMKSY).',
      icon: '🏛️',
      path: '/schemes',
      color: 'bg-purple-100 text-purple-800'
    },
    {
      title: 'Livestock & Veterinary Hub',
      description: 'Animal profiles, ear-tag tracking, 6-month vaccination scheduler, and clinical triage.',
      icon: '🐄',
      path: '/livestock',
      color: 'bg-yellow-100 text-yellow-800'
    },
    {
      title: 'Expert Tele-Consultations',
      description: 'Book video/in-person appointments with verified veterinary surgeons and agronomists.',
      icon: '🩺',
      path: '/consultations',
      color: 'bg-sky-100 text-sky-800'
    },
    {
      title: 'AI Agronomy Assistant',
      description: 'Intelligent multi-lingual chatbot with automated tool calling and clinical emergency escalation.',
      icon: '🤖',
      path: '/ai-assistant',
      color: 'bg-green-100 text-green-800'
    }
  ];

  return (
    <div className="min-h-screen bg-gray-50 pb-16">
      {/* Hero Header */}
      <div className="bg-gradient-to-r from-emerald-800 via-emerald-700 to-green-800 text-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 text-center">
          <span className="inline-block px-3 py-1 bg-emerald-600/60 rounded-full text-xs font-semibold uppercase tracking-wider mb-3">
            Smart Modular Agriculture Platform
          </span>
          <h1 className="text-4xl sm:text-5xl font-black tracking-tight">
            FarmVet Connect
          </h1>
          <p className="mt-3 text-lg text-emerald-100 max-w-2xl mx-auto">
            Unified agro-climatic intelligence, veterinary healthcare, market economics, and disease surveillance for rural producers.
          </p>

          <div className="mt-8 flex justify-center gap-4">
            {!user ? (
              <>
                <Link to="/register">
                  <Button variant="primary" size="lg" className="bg-white text-emerald-800 hover:bg-emerald-50 font-bold px-6 py-3 rounded-xl shadow-md">
                    Get Started Free
                  </Button>
                </Link>
                <Link to="/login">
                  <Button variant="outline" size="lg" className="border-white text-white hover:bg-white/10 font-bold px-6 py-3 rounded-xl">
                    Sign In
                  </Button>
                </Link>
              </>
            ) : (
              <div className="flex items-center gap-3">
                <span className="text-sm bg-emerald-900/60 px-4 py-2 rounded-xl border border-emerald-500/30">
                  Logged in as <strong>{user.username}</strong> ({user.role})
                </span>
                <Link to="/ai-assistant">
                  <Button variant="primary" size="md" className="bg-white text-emerald-800 hover:bg-emerald-50 font-bold">
                    Open AI Assistant &rarr;
                  </Button>
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Services Grid */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-12">
        <div className="flex justify-between items-end mb-8">
          <div>
            <h2 className="text-2xl font-black text-gray-900">Agro-Intelligence & Veterinary Ecosystem</h2>
            <p className="text-sm text-gray-500 mt-1">Select any module below to access live advisory and management tools.</p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {services.map((s, idx) => (
            <Link
              key={idx}
              to={s.path}
              className="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm hover:shadow-xl hover:border-emerald-300 transition-all group flex flex-col justify-between"
            >
              <div>
                <div className={`w-12 h-12 rounded-xl flex items-center justify-center text-2xl mb-4 ${s.color}`}>
                  {s.icon}
                </div>
                <h3 className="text-lg font-bold text-gray-900 group-hover:text-emerald-700 transition-colors">
                  {s.title}
                </h3>
                <p className="text-xs text-gray-600 mt-2 leading-relaxed">
                  {s.description}
                </p>
              </div>

              <div className="mt-6 flex items-center text-xs font-bold text-emerald-600 group-hover:translate-x-1 transition-transform">
                Open Service &rarr;
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
