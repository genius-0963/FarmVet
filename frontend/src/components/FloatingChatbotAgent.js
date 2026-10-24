import { useState, useRef, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';
import StreamingMarkdown from './chat/StreamingMarkdown';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function FloatingChatbotAgent() {
  const [isOpen, setIsOpen] = useState(false);
  const [aiMode, setAiMode] = useState('FREE_AUTO'); // 'FREE_AUTO', 'AGENT', 'GEMINI', 'POLLINATIONS'
  const [customKey, setCustomKey] = useState(() => localStorage.getItem('farmvet_gemini_key') || '');
  const [showKeyModal, setShowKeyModal] = useState(false);
  const [tempKeyInput, setTempKeyInput] = useState('');
  const [sessionId, setSessionId] = useState(() => {
    let sid = sessionStorage.getItem('farmvet_chat_session_id');
    if (!sid) {
      sid = 'session_' + Math.random().toString(36).substring(2, 9) + '_' + Date.now();
      sessionStorage.setItem('farmvet_chat_session_id', sid);
    }
    return sid;
  });
  const [farmerProfile, setFarmerProfile] = useState(null);

  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'agent',
      text: '🌱 **FarmVet Free AI Assistant & Agent Ready**.\n\nI am your agricultural companion. Ask me anything regarding your **crops, soil, fertilizers, irrigation, pest control, cattle health, or Mandi prices**.',
      steps: [],
      providerUsed: 'FarmVet Built-in Agro-AI'
    }
  ]);
  const [input, setInput] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const [activeSteps, setActiveSteps] = useState([]);
  const [isListening, setIsListening] = useState(false);
  const [showThoughts, setShowThoughts] = useState(true);

  const messagesEndRef = useRef(null);
  const location = useLocation();
  const navigate = useNavigate();

  // Determine current page context
  const getPageContext = () => {
    const path = location.pathname.toLowerCase();
    if (path.includes('crop')) return 'CROPS';
    if (path.includes('pesticide')) return 'PESTICIDES';
    if (path.includes('irrigation')) return 'IRRIGATION';
    if (path.includes('finance')) return 'FINANCE';
    if (path.includes('livestock')) return 'LIVESTOCK';
    if (path.includes('radar')) return 'RADAR';
    if (path.includes('scheme')) return 'SCHEMES';
    return 'GENERAL';
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    if (isOpen) {
      scrollToBottom();
    }
  }, [messages, activeSteps, isOpen]);

  // Speech-to-Text
  const startVoiceInput = () => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
      alert('Speech recognition is not supported in this browser.');
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.lang = 'en-IN';
    recognition.interimResults = false;

    recognition.onstart = () => setIsListening(true);
    recognition.onend = () => setIsListening(false);
    recognition.onerror = () => setIsListening(false);

    recognition.onresult = (event) => {
      const transcript = event.results[0][0].transcript;
      setInput(transcript);
      sendMessage(transcript);
    };

    recognition.start();
  };

  // Text-to-Speech
  const readAloud = (text) => {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();
      const clean = text.replace(/[*#_`]/g, '');
      const utterance = new SpeechSynthesisUtterance(clean);
      utterance.rate = 0.95;
      window.speechSynthesis.speak(utterance);
    }
  };

  const handleSaveKey = () => {
    localStorage.setItem('farmvet_gemini_key', tempKeyInput.trim());
    setCustomKey(tempKeyInput.trim());
    setShowKeyModal(false);
  };

  const handleResetContext = async () => {
    try {
      await axios.post(`${API_BASE}/v1/ai/session/reset?sessionId=${encodeURIComponent(sessionId)}`);
    } catch (e) {
      console.warn('Context reset failed', e);
    }
    setFarmerProfile(null);
    const newSid = 'session_' + Math.random().toString(36).substring(2, 9) + '_' + Date.now();
    sessionStorage.setItem('farmvet_chat_session_id', newSid);
    setSessionId(newSid);
    setMessages((prev) => [
      ...prev,
      {
        id: Date.now(),
        sender: 'agent',
        text: '🔄 **Field Context Reset**. You can now discuss a new crop, parcel, or livestock herd without carrying over prior crop history.',
        providerUsed: 'FarmVet Copilot'
      }
    ]);
  };

  const sendMessage = async (queryText) => {
    const textToSend = queryText || input;
    if (!textToSend.trim() || isStreaming) return;

    const userMsg = { id: Date.now(), sender: 'user', text: textToSend };
    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setIsStreaming(true);
    setActiveSteps([]);

    const token = localStorage.getItem('token') || '';
    const pageContext = getPageContext();
    const agentMsgId = Date.now() + 1;

    const initialAgentMsg = {
      id: agentMsgId,
      sender: 'agent',
      text: '',
      steps: [],
      nextActions: [],
      fallback: false,
      providerUsed: aiMode === 'AGENT' ? 'Autonomous ReAct Agent' : 'Free AI Engine'
    };

    setMessages((prev) => [...prev, initialAgentMsg]);

    // Choose stream endpoint based on selected AI Mode
    if (aiMode === 'AGENT') {
      const streamUrl = `${API_BASE}/v1/ai/agent/stream?prompt=${encodeURIComponent(textToSend)}&pageContext=${encodeURIComponent(pageContext)}&token=${encodeURIComponent(token)}`;

      try {
        const eventSource = new EventSource(streamUrl);

        eventSource.addEventListener('agent_thought', (e) => {
          try {
            const stepData = JSON.parse(e.data);
            setActiveSteps((prev) => [...prev, stepData]);
            setMessages((prev) =>
              prev.map((m) =>
                m.id === agentMsgId
                  ? { ...m, steps: [...(m.steps || []), stepData] }
                  : m
              )
            );
          } catch (err) {
            console.warn('Error parsing thought step', err);
          }
        });

        eventSource.addEventListener('agent_token', (e) => {
          setMessages((prev) =>
            prev.map((m) =>
              m.id === agentMsgId ? { ...m, text: m.text + e.data } : m
            )
          );
        });

        eventSource.addEventListener('agent_complete', (e) => {
          try {
            const finalData = JSON.parse(e.data);
            setMessages((prev) =>
              prev.map((m) =>
                m.id === agentMsgId
                  ? {
                      ...m,
                      nextActions: finalData.suggestedNextActions || [],
                      fallback: finalData.emergencyFallback,
                      providerUsed: 'Autonomous ReAct Copilot'
                    }
                  : m
              )
            );
          } catch (err) {
            console.warn('Error parsing agent_complete event', err);
          }
          eventSource.close();
          setIsStreaming(false);
        });

        eventSource.onerror = async () => {
          eventSource.close();
          // Fallback to synchronous execution
          try {
            const res = await axios.post(
              `${API_BASE}/v1/ai/agent/execute`,
              { prompt: textToSend, pageContext },
              { headers: { Authorization: `Bearer ${token}` } }
            );
            if (res.data?.data) {
              const d = res.data.data;
              setMessages((prev) =>
                prev.map((m) =>
                  m.id === agentMsgId
                    ? {
                        ...m,
                        text: d.agentReply,
                        steps: d.agentSteps || [],
                        nextActions: d.suggestedNextActions || [],
                        fallback: d.emergencyFallback,
                        providerUsed: 'Autonomous Copilot (Direct)'
                      }
                    : m
                )
              );
            }
          } catch (fallbackErr) {
            setMessages((prev) =>
              prev.map((m) =>
                m.id === agentMsgId
                  ? { ...m, text: 'Agent error processing query. Please check your network connection.' }
                  : m
              )
            );
          } finally {
            setIsStreaming(false);
          }
        };
      } catch (e) {
        setIsStreaming(false);
      }
    } else {
      // Free AI Modes: FREE_AUTO, GEMINI, POLLINATIONS
      const providerParam = aiMode === 'FREE_AUTO' ? 'AUTO' : aiMode;
      const streamUrl = `${API_BASE}/v1/ai/free-stream?prompt=${encodeURIComponent(textToSend)}&provider=${encodeURIComponent(providerParam)}&customApiKey=${encodeURIComponent(customKey)}&pageContext=${encodeURIComponent(pageContext)}&sessionId=${encodeURIComponent(sessionId)}&token=${encodeURIComponent(token)}`;

      try {
        const eventSource = new EventSource(streamUrl);

        eventSource.addEventListener('status', (e) => {
          setActiveSteps([{ thought: e.data }]);
        });

        eventSource.addEventListener('token', (e) => {
          setMessages((prev) =>
            prev.map((m) =>
              m.id === agentMsgId ? { ...m, text: m.text + e.data } : m
            )
          );
        });

        eventSource.addEventListener('guardrail_refusal', (e) => {
          setMessages((prev) =>
            prev.map((m) =>
              m.id === agentMsgId ? { ...m, text: e.data, providerUsed: 'Domain Guardrail' } : m
            )
          );
        });

        eventSource.addEventListener('complete', (e) => {
          try {
            const meta = JSON.parse(e.data);
            if (meta.farmerProfile && (meta.farmerProfile.crop || meta.farmerProfile.acreage)) {
              setFarmerProfile(meta.farmerProfile);
            }
            setMessages((prev) =>
              prev.map((m) =>
                m.id === agentMsgId
                  ? {
                      ...m,
                      providerUsed: meta.providerUsed || 'Free AI Engine',
                      cacheHit: meta.cacheHit,
                      latencyMs: meta.latencyMs,
                      safetyNotice: meta.safetyNotice
                    }
                  : m
              )
            );
          } catch (err) {
            console.warn('Error parsing complete', err);
          }
          eventSource.close();
          setIsStreaming(false);
          setActiveSteps([]);
        });

        eventSource.onerror = async () => {
          eventSource.close();
          // Fallback to sync free-chat POST
          try {
            const res = await axios.post(
              `${API_BASE}/v1/ai/free-chat`,
              {
                prompt: textToSend,
                provider: providerParam,
                customApiKey: customKey,
                pageContext,
                sessionId
              },
              { headers: { Authorization: `Bearer ${token}` } }
            );
            if (res.data?.data) {
              const d = res.data.data;
              if (d.farmerProfile && (d.farmerProfile.crop || d.farmerProfile.acreage)) {
                setFarmerProfile(d.farmerProfile);
              }
              setMessages((prev) =>
                prev.map((m) =>
                  m.id === agentMsgId
                    ? {
                        ...m,
                        text: d.reply,
                        providerUsed: d.providerUsed,
                        cacheHit: d.cacheHit,
                        latencyMs: d.latencyMs,
                        safetyNotice: d.safetyNotice
                      }
                    : m
                )
              );
            }
          } catch (err) {
            setMessages((prev) =>
              prev.map((m) =>
                m.id === agentMsgId
                  ? { ...m, text: 'Could not connect to free AI service. Please try again.' }
                  : m
              )
            );
          } finally {
            setIsStreaming(false);
            setActiveSteps([]);
          }
        };
      } catch (e) {
        setIsStreaming(false);
        setActiveSteps([]);
      }
    }
  };

  // Context-sensitive prompt recommendations
  const getContextChips = () => {
    const ctx = getPageContext();
    switch (ctx) {
      case 'CROPS':
        return ['Wheat NPK fertilizer schedule', 'Soybean basal seed treatment', 'Crop lifecycle stages'];
      case 'PESTICIDES':
        return ['Check Mancozeb safe harvest days (PHI)', 'Neem oil dosage for cotton whitefly', 'Pesticide spray rules'];
      case 'IRRIGATION':
        return ['Do I need to irrigate today?', 'Optimal drip timing for summer', 'Calculate soil moisture deficit'];
      case 'FINANCE':
        return ['Today APMC Mandi price for Soybean', 'Wheat modal price in Indore', 'How to increase crop profit margin'];
      case 'LIVESTOCK':
        return ['Cow has fever and blisters', 'Mastitis milk clot symptoms', 'FMD 6-month vaccination booster'];
      default:
        return ['Check today Mandi rates', 'Recommend fertilizer for wheat', 'Livestock disease triage', 'PM-KISAN eligibility'];
    }
  };

  return (
    <>
      {/* Floating Action Button (FAB) */}
      {!isOpen && (
        <button
          type="button"
          onClick={() => setIsOpen(true)}
          className="fixed bottom-6 right-6 z-50 bg-gradient-to-tr from-emerald-600 to-green-500 text-white p-4 rounded-full shadow-2xl hover:scale-105 active:scale-95 transition-all flex items-center gap-2 group ring-4 ring-emerald-300/40"
          title="Open FarmVet Autonomous Copilot Agent & Free AI"
        >
          <span className="text-2xl animate-bounce">🌱</span>
          <span className="font-bold text-xs pr-1 hidden sm:inline-block">Farm AI</span>
          <span className="w-2.5 h-2.5 rounded-full bg-emerald-200 animate-ping absolute -top-1 -right-1"></span>
        </button>
      )}

      {/* Floating Agent Dialog */}
      {isOpen && (
        <div className="fixed bottom-6 right-6 z-50 w-[95vw] sm:w-[480px] h-[640px] max-h-[88vh] bg-white rounded-3xl shadow-2xl border border-gray-200 flex flex-col overflow-hidden animate-in fade-in slide-in-from-bottom-6 duration-200">
          {/* Header */}
          <div className="bg-gradient-to-r from-emerald-800 to-green-700 text-white p-3.5 flex items-center justify-between shrink-0 shadow-sm">
            <div className="flex items-center gap-2.5">
              <div className="bg-emerald-700/80 p-2 rounded-xl text-lg">🌱</div>
              <div>
                <div className="flex items-center gap-2">
                  <h3 className="text-sm font-extrabold tracking-tight">FarmVet Copilot</h3>
                  <span className="text-[10px] bg-emerald-600/80 px-2 py-0.5 rounded-full text-emerald-200 uppercase font-semibold">
                    {getPageContext()}
                  </span>
                </div>
                <div className="flex items-center gap-1.5 text-[11px] text-emerald-200 mt-0.5">
                  <span className="w-2 h-2 rounded-full bg-green-400 animate-pulse"></span>
                  <span>100% Crop & Farm Restricted</span>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-1">
              <button
                type="button"
                onClick={() => {
                  setTempKeyInput(customKey);
                  setShowKeyModal(true);
                }}
                className={`px-2 py-1 rounded-lg text-xs font-semibold transition-colors flex items-center gap-1 ${
                  customKey ? 'bg-amber-500/30 text-amber-200' : 'bg-emerald-700/60 text-emerald-100 hover:bg-emerald-700'
                }`}
                title="Manage AI API Keys"
              >
                <span>{customKey ? '🔑 Custom Key' : '⚡ Free AI Active'}</span>
              </button>
              {aiMode === 'AGENT' && (
                <button
                  type="button"
                  onClick={() => setShowThoughts(!showThoughts)}
                  className={`p-1.5 rounded-lg text-xs font-semibold transition-colors ${
                    showThoughts ? 'bg-emerald-700 text-emerald-100' : 'text-emerald-300 hover:bg-emerald-700/50'
                  }`}
                  title="Toggle ReAct thoughts accordion"
                >
                  🧠 ReAct
                </button>
              )}
              <button
                type="button"
                onClick={() => setIsOpen(false)}
                className="p-1.5 hover:bg-emerald-700/60 rounded-lg text-emerald-200 hover:text-white transition-colors"
                title="Minimize Agent"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M19 9l-7 7-7-7" />
                </svg>
              </button>
            </div>
          </div>

          {/* AI Mode Selector Bar */}
          <div className="bg-emerald-950/90 text-white px-3 py-1.5 flex items-center justify-between text-[11px] shrink-0 border-b border-emerald-900">
            <span className="text-emerald-300 font-bold shrink-0">Model:</span>
            <div className="flex items-center gap-1 overflow-x-auto no-scrollbar">
              <button
                type="button"
                onClick={() => setAiMode('FREE_AUTO')}
                className={`px-2 py-0.5 rounded-full font-medium transition-colors shrink-0 ${
                  aiMode === 'FREE_AUTO' ? 'bg-emerald-500 text-white font-bold' : 'text-emerald-200 hover:bg-emerald-900'
                }`}
                title="FarmVet Built-in Free AI (Instant, zero-cost, no key required)"
              >
                ⚡ Free Agro-AI
              </button>
              <button
                type="button"
                onClick={() => setAiMode('AGENT')}
                className={`px-2 py-0.5 rounded-full font-medium transition-colors shrink-0 ${
                  aiMode === 'AGENT' ? 'bg-emerald-500 text-white font-bold' : 'text-emerald-200 hover:bg-emerald-900'
                }`}
                title="Autonomous ReAct Agent with Tool Execution"
              >
                🤖 ReAct Agent
              </button>
              <button
                type="button"
                onClick={() => setAiMode('GEMINI')}
                className={`px-2 py-0.5 rounded-full font-medium transition-colors shrink-0 ${
                  aiMode === 'GEMINI' ? 'bg-emerald-500 text-white font-bold' : 'text-emerald-200 hover:bg-emerald-900'
                }`}
                title="Google Gemini 1.5 Flash Free Tier"
              >
                🌟 Gemini Free
              </button>
              <button
                type="button"
                onClick={() => setAiMode('POLLINATIONS')}
                className={`px-2 py-0.5 rounded-full font-medium transition-colors shrink-0 ${
                  aiMode === 'POLLINATIONS' ? 'bg-emerald-500 text-white font-bold' : 'text-emerald-200 hover:bg-emerald-900'
                }`}
                title="Open-Source Free LLM"
              >
                🦙 Open LLM
              </button>
            </div>
          </div>

          {/* Active Field Context Banner */}
          {farmerProfile && (farmerProfile.crop || farmerProfile.acreage) && (
            <div className="bg-emerald-900 text-white px-3 py-1.5 flex items-center justify-between text-[11px] shrink-0 border-b border-emerald-800">
              <div className="flex items-center gap-1.5 overflow-hidden">
                <span className="text-emerald-300 font-bold shrink-0">🌱 Field Memory:</span>
                <span className="bg-emerald-800 px-2 py-0.5 rounded text-emerald-100 font-bold truncate">
                  {farmerProfile.crop || 'Crop'}
                </span>
                {farmerProfile.acreage && (
                  <span className="text-emerald-200 shrink-0">
                    {farmerProfile.acreage} Ac
                  </span>
                )}
                {farmerProfile.cropStage && (
                  <span className="text-emerald-300 hidden sm:inline truncate">
                    • {farmerProfile.cropStage}
                  </span>
                )}
              </div>
              <button
                type="button"
                onClick={handleResetContext}
                className="text-emerald-200 hover:text-white underline font-bold shrink-0 ml-2"
                title="Reset field context"
              >
                Reset Field
              </button>
            </div>
          )}

          {/* Quick Context Chips */}
          <div className="p-2 bg-gray-50 border-b border-gray-100 flex items-center gap-1.5 overflow-x-auto no-scrollbar shrink-0">
            <span className="text-[10px] text-gray-500 font-bold shrink-0">Suggestions:</span>
            {getContextChips().map((chip, idx) => (
              <button
                key={idx}
                type="button"
                disabled={isStreaming}
                onClick={() => sendMessage(chip)}
                className="text-[11px] px-2.5 py-1 bg-white hover:bg-emerald-50 text-emerald-800 hover:text-emerald-900 rounded-full font-medium border border-gray-200 shrink-0 whitespace-nowrap shadow-2xs"
              >
                {chip}
              </button>
            ))}
          </div>

          {/* Messages Stream */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50/30">
            {messages.map((m) => (
              <div
                key={m.id}
                className={`flex ${m.sender === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                <div
                  className={`max-w-[88%] rounded-2xl p-3.5 text-xs leading-relaxed shadow-2xs ${
                    m.sender === 'user'
                      ? 'bg-emerald-600 text-white rounded-br-none'
                      : 'bg-white text-gray-900 rounded-bl-none border border-gray-200'
                  }`}
                >
                  {/* Read Aloud & Provider Badge for Agent */}
                  {m.sender === 'agent' && (
                    <div className="flex justify-between items-center pb-1.5 mb-1.5 border-b border-gray-100 text-[10px] text-gray-400 font-semibold">
                      <div className="flex items-center gap-1.5 flex-wrap">
                        <span className="text-emerald-700 bg-emerald-50 px-1.5 py-0.5 rounded border border-emerald-100 font-medium">
                          {m.providerUsed || 'FarmVet AI'}
                        </span>
                        {m.cacheHit && (
                          <span className="text-amber-700 bg-amber-50 px-1.5 py-0.5 rounded border border-amber-200 font-bold">
                            ⚡ Cache ({m.latencyMs || '<5'}ms)
                          </span>
                        )}
                      </div>
                      {m.text && (
                        <button
                          type="button"
                          onClick={() => readAloud(m.text)}
                          className="hover:text-emerald-700 flex items-center gap-1 shrink-0"
                        >
                          <span>🔊</span> Listen
                        </button>
                      )}
                    </div>
                  )}

                  {/* Regulatory Safety Notice Banner */}
                  {m.safetyNotice && (
                    <div className="mb-2 p-2 rounded-xl bg-amber-50 border border-amber-200 text-amber-900 text-[11px] flex items-start gap-1.5 font-medium">
                      <span className="text-sm shrink-0">🛡️</span>
                      <div>{m.safetyNotice}</div>
                    </div>
                  )}

                  {/* Expandable ReAct Thought Steps */}
                  {showThoughts && m.steps && m.steps.length > 0 && (
                    <details className="mb-2.5 p-2 bg-emerald-50/70 rounded-xl border border-emerald-100 text-[11px] text-emerald-900">
                      <summary className="font-bold cursor-pointer select-none">
                        🧠 ReAct Agent Execution Steps ({m.steps.length})
                      </summary>
                      <div className="mt-2 space-y-1.5 pl-2 border-l border-emerald-300">
                        {m.steps.map((st, sidx) => (
                          <div key={sidx} className="space-y-0.5">
                            <div className="font-bold text-emerald-950">
                              #{sidx + 1} {st.stepName}
                            </div>
                            <div className="text-[10px] text-emerald-800">
                              <strong>Action:</strong> <code>{st.action}</code>
                            </div>
                            <div className="text-[10px] text-emerald-700">
                              <strong>Result:</strong> {st.observation}
                            </div>
                          </div>
                        ))}
                      </div>
                    </details>
                  )}

                  {/* Message Content with Streaming Markdown Parser */}
                  {m.sender === 'user' ? (
                    <div className="whitespace-pre-line font-medium text-white">{m.text}</div>
                  ) : (
                    <div>
                      {m.text ? (
                        <StreamingMarkdown
                          content={m.text}
                          isStreaming={isStreaming && m.id === messages[messages.length - 1].id}
                        />
                      ) : (
                        <span className="text-gray-400 italic">Formulating agro-intelligence plan...</span>
                      )}
                      {isStreaming && m.id === messages[messages.length - 1].id && (
                        <span className="inline-block w-1.5 h-3.5 ml-1 bg-emerald-600 animate-pulse align-middle"></span>
                      )}
                    </div>
                  )}

                  {/* Emergency Escalation */}
                  {m.fallback && (
                    <div className="mt-2.5 p-2.5 bg-rose-50 border border-rose-200 rounded-xl text-[11px] text-rose-900 font-semibold">
                      🚨 <strong>Clinical Emergency:</strong> Immediate veterinary visit recommended.
                      <button
                        type="button"
                        onClick={() => {
                          setIsOpen(false);
                          navigate('/consultations');
                        }}
                        className="block mt-1 text-rose-700 underline font-bold"
                      >
                        Book Video Doctor Call &rarr;
                      </button>
                    </div>
                  )}

                  {/* Dynamic Next Actions & Suggestion Chips */}
                  {m.sender === 'agent' && (
                    <div className="mt-3 pt-2.5 border-t border-gray-100">
                      <div className="text-[10px] font-bold text-gray-400 uppercase mb-1.5 flex items-center gap-1">
                        <span>⚡</span>
                        <span>{m.nextActions && m.nextActions.length > 0 ? 'Suggested Next Actions:' : 'Quick Prompts:'}</span>
                      </div>
                      <div className="flex flex-wrap gap-1.5">
                        {(m.nextActions && m.nextActions.length > 0
                          ? m.nextActions
                          : ['🌾 Wheat fertilizer schedule', '🐛 Tomato pest remedy', '💧 Smart irrigation liters', '💰 Mandi soybean price']
                        ).map((act, actIdx) => (
                          <button
                            key={actIdx}
                            type="button"
                            disabled={isStreaming}
                            onClick={() => sendMessage(act)}
                            className="text-[10px] px-2.5 py-1 bg-emerald-50/80 hover:bg-emerald-100 text-emerald-900 rounded-lg font-semibold border border-emerald-200 transition-colors shadow-2xs"
                          >
                            {act} &rarr;
                          </button>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ))}

            {isStreaming && activeSteps.length > 0 && (
              <div className="flex justify-start">
                <div className="bg-emerald-50 border border-emerald-200 text-emerald-900 rounded-xl p-2 text-[11px] flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full border-2 border-emerald-600 border-t-transparent animate-spin"></span>
                  <span>{activeSteps[activeSteps.length - 1].thought || 'Consulting agronomy models...'}</span>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Input Bar */}
          <div className="p-3 bg-white border-t border-gray-100">
            <form
              onSubmit={(e) => {
                e.preventDefault();
                sendMessage();
              }}
              className="flex items-center gap-1.5"
            >
              <button
                type="button"
                onClick={startVoiceInput}
                disabled={isStreaming}
                className={`p-2.5 rounded-xl text-sm transition-all ${
                  isListening
                    ? 'bg-rose-600 text-white animate-pulse'
                    : 'bg-gray-100 hover:bg-gray-200 text-gray-700'
                }`}
                title="Voice dictation (Speech to Text)"
              >
                🎤
              </button>

              <input
                type="text"
                placeholder="Ask your Farm Copilot..."
                value={input}
                onChange={(e) => setInput(e.target.value)}
                disabled={isStreaming}
                className="flex-1 px-3.5 py-2.5 border border-gray-300 rounded-xl focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-xs outline-none"
              />

              <button
                type="submit"
                disabled={isStreaming || !input.trim()}
                className="bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white font-bold p-2.5 rounded-xl shadow-md transition-all shrink-0"
                title="Send query"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                </svg>
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Free AI API Key Modal (Google, Groq, OpenRouter) */}
      {showKeyModal && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl p-6 max-w-lg w-full shadow-2xl border border-gray-100 animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between pb-3 border-b border-gray-100">
              <div className="flex items-center gap-2">
                <span className="text-xl">🔑</span>
                <h3 className="font-bold text-gray-900 text-sm">Connect Any Free AI Provider API</h3>
              </div>
              <button
                type="button"
                onClick={() => setShowKeyModal(false)}
                className="text-gray-400 hover:text-gray-600 text-lg font-bold"
              >
                &times;
              </button>
            </div>

            <div className="mt-4 space-y-3 text-xs text-gray-600">
              <p>
                FarmVet automatically detects and connects to your free API based on key prefix:
              </p>
              <div className="grid grid-cols-1 gap-2">
                <div className="p-2.5 bg-emerald-50 text-emerald-950 rounded-xl border border-emerald-200">
                  <div className="font-bold flex items-center justify-between">
                    <span>🌟 Google Gemini 1.5 Flash</span>
                    <span className="text-[10px] bg-emerald-200 px-1.5 py-0.5 rounded font-mono">AIzaSy...</span>
                  </div>
                  <p className="text-[11px] text-emerald-800 mt-0.5">
                    15 requests/min, 1,500/day. Free at <strong>aistudio.google.com</strong> (no card needed).
                  </p>
                </div>

                <div className="p-2.5 bg-amber-50 text-amber-950 rounded-xl border border-amber-200">
                  <div className="font-bold flex items-center justify-between">
                    <span>⚡ Groq Cloud (LLaMA 3.3 70B)</span>
                    <span className="text-[10px] bg-amber-200 px-1.5 py-0.5 rounded font-mono">gsk_...</span>
                  </div>
                  <p className="text-[11px] text-amber-800 mt-0.5">
                    30 requests/min, ultra-fast 500 tokens/sec. Free at <strong>console.groq.com</strong>.
                  </p>
                </div>

                <div className="p-2.5 bg-indigo-50 text-indigo-950 rounded-xl border border-indigo-200">
                  <div className="font-bold flex items-center justify-between">
                    <span>🦙 OpenRouter Free Tier</span>
                    <span className="text-[10px] bg-indigo-200 px-1.5 py-0.5 rounded font-mono">sk-or-...</span>
                  </div>
                  <p className="text-[11px] text-indigo-800 mt-0.5">
                    20+ open-source free models. Free at <strong>openrouter.ai</strong>.
                  </p>
                </div>
              </div>

              <div className="pt-2">
                <label className="block font-bold text-gray-700 mb-1">Paste Your Free API Key:</label>
                <input
                  type="password"
                  placeholder="Paste AIza..., gsk_..., or sk-or-... key"
                  value={tempKeyInput}
                  onChange={(e) => setTempKeyInput(e.target.value)}
                  className="w-full px-3.5 py-2.5 border border-gray-300 rounded-xl text-xs font-mono focus:ring-2 focus:ring-emerald-500 outline-none"
                />
                <p className="text-[10px] text-gray-400 mt-1">
                  🔒 Keys are stored strictly in your browser&apos;s local storage and sent only to the AI provider.
                </p>
              </div>
            </div>

            <div className="mt-5 flex justify-between items-center pt-3 border-t border-gray-100">
              <button
                type="button"
                onClick={() => {
                  setTempKeyInput('');
                  localStorage.removeItem('farmvet_gemini_key');
                  setCustomKey('');
                  setShowKeyModal(false);
                }}
                className="px-3 py-1.5 text-xs text-rose-600 hover:bg-rose-50 rounded-xl font-medium"
              >
                Use Built-in Free AI
              </button>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setShowKeyModal(false)}
                  className="px-3 py-1.5 text-xs text-gray-600 hover:bg-gray-100 rounded-xl font-medium"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleSaveKey}
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-xl shadow-md transition-colors"
                >
                  Save & Connect
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
