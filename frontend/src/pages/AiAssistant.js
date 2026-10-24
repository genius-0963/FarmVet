import { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import Button from '../components/ui/Button';
import StreamingMarkdown from '../components/chat/StreamingMarkdown';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8082/api';

export default function AiAssistant() {
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'ai',
      text: '🌾 Namaste Farmer! I am your real-time **FarmVet AI Agronomy & Veterinary Assistant**.\n\nI am exclusively specialized in **crops, field soils, irrigation, fertilizers, pest control, livestock healthcare, and Mandi market rates**.\n\nHow can I help your farm or herd today?',
      providerUsed: 'FarmVet Built-in Agro-AI'
    }
  ]);
  const [input, setInput] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const [activeToolSteps, setActiveToolSteps] = useState([]);
  const [isListening, setIsListening] = useState(false);
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
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, activeToolSteps]);

  // Speech-to-Text for Rural Farmers
  const startVoiceInput = () => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
      alert('Speech recognition is not supported in this browser. Please type your query.');
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

  // Text-to-Speech Read Aloud for Farmers
  const readAloud = (text) => {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();
      const cleanText = text.replace(/[*#_`]/g, '');
      const utterance = new SpeechSynthesisUtterance(cleanText);
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
        sender: 'ai',
        text: '🔄 **Field Context Reset**. You can now discuss a new crop, acreage, or livestock herd without carrying over prior context.',
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
    setActiveToolSteps([]);

    const token = localStorage.getItem('token') || '';
    const aiMsgId = Date.now() + 1;
    const initialAiMsg = {
      id: aiMsgId,
      sender: 'ai',
      text: '',
      tools: [],
      steps: [],
      fallback: false,
      speciality: null,
      providerUsed: aiMode === 'AGENT' ? 'Autonomous ReAct Agent' : 'Free AI Engine'
    };

    setMessages((prev) => [...prev, initialAiMsg]);

    // Check Mode: Agent vs Free AI
    if (aiMode === 'AGENT') {
      const streamUrl = `${API_BASE}/v1/ai/agent/stream?prompt=${encodeURIComponent(textToSend)}&pageContext=GENERAL&token=${encodeURIComponent(token)}`;

      try {
        const eventSource = new EventSource(streamUrl);

        eventSource.addEventListener('agent_thought', (e) => {
          try {
            const stepData = JSON.parse(e.data);
            setActiveToolSteps((prev) => [...prev, stepData.thought || stepData.stepName]);
            setMessages((prev) =>
              prev.map((m) =>
                m.id === aiMsgId
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
              m.id === aiMsgId ? { ...m, text: m.text + e.data } : m
            )
          );
        });

        eventSource.addEventListener('agent_complete', (e) => {
          try {
            const finalData = JSON.parse(e.data);
            setMessages((prev) =>
              prev.map((m) =>
                m.id === aiMsgId
                  ? {
                      ...m,
                      fallback: finalData.emergencyFallback,
                      providerUsed: 'Autonomous ReAct Copilot'
                    }
                  : m
              )
            );
          } catch (err) {
            console.warn('Error parsing complete', err);
          }
          eventSource.close();
          setIsStreaming(false);
          setActiveToolSteps([]);
        });

        eventSource.onerror = async () => {
          eventSource.close();
          try {
            const res = await axios.post(
              `${API_BASE}/v1/ai/agent/execute`,
              { prompt: textToSend, pageContext: 'GENERAL' },
              { headers: { Authorization: `Bearer ${token}` } }
            );
            if (res.data?.data) {
              const d = res.data.data;
              setMessages((prev) =>
                prev.map((m) =>
                  m.id === aiMsgId
                    ? {
                        ...m,
                        text: d.agentReply,
                        steps: d.agentSteps || [],
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
                m.id === aiMsgId
                  ? { ...m, text: 'Agent execution failed. Please verify network connectivity.' }
                  : m
              )
            );
          } finally {
            setIsStreaming(false);
            setActiveToolSteps([]);
          }
        };
      } catch (e) {
        setIsStreaming(false);
        setActiveToolSteps([]);
      }
    } else {
      // Free AI Modes: FREE_AUTO, GEMINI, POLLINATIONS
      const providerParam = aiMode === 'FREE_AUTO' ? 'AUTO' : aiMode;
      const streamUrl = `${API_BASE}/v1/ai/free-stream?prompt=${encodeURIComponent(textToSend)}&provider=${encodeURIComponent(providerParam)}&customApiKey=${encodeURIComponent(customKey)}&pageContext=GENERAL&sessionId=${encodeURIComponent(sessionId)}&token=${encodeURIComponent(token)}`;

      try {
        const eventSource = new EventSource(streamUrl);

        eventSource.addEventListener('status', (e) => {
          setActiveToolSteps([e.data]);
        });

        eventSource.addEventListener('token', (e) => {
          setMessages((prev) =>
            prev.map((m) =>
              m.id === aiMsgId ? { ...m, text: m.text + e.data } : m
            )
          );
        });

        eventSource.addEventListener('guardrail_refusal', (e) => {
          setMessages((prev) =>
            prev.map((m) =>
              m.id === aiMsgId ? { ...m, text: e.data, providerUsed: 'Domain Guardrail' } : m
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
                m.id === aiMsgId
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
          setActiveToolSteps([]);
        });

        eventSource.onerror = async () => {
          eventSource.close();
          try {
            const res = await axios.post(
              `${API_BASE}/v1/ai/free-chat`,
              {
                prompt: textToSend,
                provider: providerParam,
                customApiKey: customKey,
                pageContext: 'GENERAL',
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
                  m.id === aiMsgId
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
          } catch (fallbackErr) {
            setMessages((prev) =>
              prev.map((m) =>
                m.id === aiMsgId
                  ? { ...m, text: 'Apologies, could not process request right now. Please verify network connectivity.' }
                  : m
              )
            );
          } finally {
            setIsStreaming(false);
            setActiveToolSteps([]);
          }
        };
      } catch (e) {
        setIsStreaming(false);
        setActiveToolSteps([]);
      }
    }
  };

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      {/* Header Banner */}
      <div className="mb-6 flex flex-col md:flex-row md:items-center justify-between gap-4 bg-emerald-900 text-white p-6 rounded-3xl shadow-lg border border-emerald-800">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="p-1.5 bg-emerald-800 rounded-lg text-lg">🌱</span>
            <span className="text-xs font-bold uppercase tracking-wider bg-emerald-700/60 text-emerald-200 px-2.5 py-0.5 rounded-full border border-emerald-600">
              100% Crop & Farm Restricted AI
            </span>
            <span className="flex items-center gap-1 text-[11px] bg-green-500/20 text-green-300 px-2 py-0.5 rounded-full">
              <span className="w-2 h-2 rounded-full bg-green-400 animate-pulse"></span>
              Live SSE Stream
            </span>
          </div>
          <h1 className="text-2xl font-black tracking-tight">FarmVet AI Agronomy & Veterinary Assistant</h1>
          <p className="text-xs text-emerald-200 mt-1 max-w-2xl">
            Real-time agro-intelligence engine with domain guardrails. Exclusively answers questions regarding crops, soil fertility, irrigation, livestock health, pesticides, and APMC Mandi rates.
          </p>
        </div>

        <div className="flex items-center gap-2 shrink-0">
          <button
            type="button"
            onClick={() => {
              setTempKeyInput(customKey);
              setShowKeyModal(true);
            }}
            className={`px-3 py-2.5 rounded-xl font-bold text-xs flex items-center gap-1.5 transition-all shadow-md ${
              customKey ? 'bg-amber-500 text-white' : 'bg-emerald-800 hover:bg-emerald-700 text-emerald-100'
            }`}
            title="Manage Free AI API Keys"
          >
            <span>{customKey ? '🔑 Key Active' : '⚡ Free AI Active'}</span>
          </button>

          <button
            type="button"
            onClick={startVoiceInput}
            className={`px-4 py-2.5 rounded-xl font-bold text-xs flex items-center gap-2 transition-all shadow-md ${
              isListening
                ? 'bg-rose-600 text-white animate-bounce'
                : 'bg-emerald-600 hover:bg-emerald-500 text-white'
            }`}
          >
            <span>🎤</span>
            {isListening ? 'Listening...' : 'Voice Query'}
          </button>
        </div>
      </div>

      {/* Model Selection Tabs */}
      <div className="bg-white p-2 rounded-2xl shadow-sm border border-gray-200 mb-4 flex items-center justify-between flex-wrap gap-2">
        <div className="flex items-center gap-1 text-xs">
          <span className="font-bold text-gray-500 px-2">Engine Mode:</span>
          <button
            type="button"
            onClick={() => setAiMode('FREE_AUTO')}
            className={`px-3 py-1.5 rounded-xl font-semibold transition-all ${
              aiMode === 'FREE_AUTO'
                ? 'bg-emerald-600 text-white shadow-xs'
                : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            ⚡ FarmVet Free Agro-AI
          </button>
          <button
            type="button"
            onClick={() => setAiMode('AGENT')}
            className={`px-3 py-1.5 rounded-xl font-semibold transition-all ${
              aiMode === 'AGENT'
                ? 'bg-emerald-600 text-white shadow-xs'
                : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            🤖 Autonomous ReAct Agent
          </button>
          <button
            type="button"
            onClick={() => setAiMode('GEMINI')}
            className={`px-3 py-1.5 rounded-xl font-semibold transition-all ${
              aiMode === 'GEMINI'
                ? 'bg-emerald-600 text-white shadow-xs'
                : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            🌟 Google Gemini Free
          </button>
          <button
            type="button"
            onClick={() => setAiMode('POLLINATIONS')}
            className={`px-3 py-1.5 rounded-xl font-semibold transition-all ${
              aiMode === 'POLLINATIONS'
                ? 'bg-emerald-600 text-white shadow-xs'
                : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            🦙 Open-Source Free LLM
          </button>
        </div>

        <div className="text-[11px] text-emerald-800 bg-emerald-50 px-2.5 py-1 rounded-lg border border-emerald-100 font-medium hidden sm:block">
          {aiMode === 'FREE_AUTO' && '⚡ 100% Free • Unlimited • Zero Latency • No Setup'}
          {aiMode === 'AGENT' && '🤖 ReAct Reasoning • Tool Execution • Automated Workflow'}
          {aiMode === 'GEMINI' && '🌟 Gemini 1.5 Flash • 15 RPM Free Tier • Rich Explanations'}
          {aiMode === 'POLLINATIONS' && '🦙 Open-Source LLM • Public Free Tier API'}
        </div>
      </div>

      {/* Active Field Context Banner */}
      {farmerProfile && (farmerProfile.crop || farmerProfile.acreage) && (
        <div className="bg-emerald-900 text-white px-4 py-2.5 rounded-2xl shadow-sm border border-emerald-800 mb-4 flex items-center justify-between text-xs">
          <div className="flex items-center gap-2 overflow-hidden">
            <span className="p-1 bg-emerald-800 rounded-lg text-sm">🌱</span>
            <span className="text-emerald-300 font-bold shrink-0">Field Memory Context:</span>
            <span className="bg-emerald-800 px-2.5 py-0.5 rounded-md font-bold text-emerald-100">
              {farmerProfile.crop || 'Crop'}
            </span>
            {farmerProfile.acreage && (
              <span className="text-emerald-200">
                {farmerProfile.acreage} Acres
              </span>
            )}
            {farmerProfile.cropStage && (
              <span className="text-emerald-300 hidden sm:inline">
                • Stage: {farmerProfile.cropStage}
              </span>
            )}
            {farmerProfile.soilType && (
              <span className="text-emerald-300 hidden md:inline">
                • Soil: {farmerProfile.soilType}
              </span>
            )}
          </div>
          <button
            type="button"
            onClick={handleResetContext}
            className="text-emerald-200 hover:text-white underline font-bold shrink-0 ml-3"
            title="Clear field memory"
          >
            Reset Field
          </button>
        </div>
      )}

      {/* Farm Topics Quick Pills */}
      <div className="flex flex-wrap gap-2 mb-4">
        <span className="text-xs text-gray-500 self-center font-semibold">Agro Prompts:</span>
        <button
          type="button"
          disabled={isStreaming}
          onClick={() => sendMessage('Tell me the fertilizer and CRI irrigation schedule for wheat')}
          className="text-xs px-3 py-1.5 bg-emerald-50 text-emerald-800 hover:bg-emerald-100 rounded-full font-medium border border-emerald-200"
        >
          🌾 Wheat CRI Irrigation
        </button>
        <button
          type="button"
          disabled={isStreaming}
          onClick={() => sendMessage('How to manage pink bollworm and whitefly in cotton?')}
          className="text-xs px-3 py-1.5 bg-indigo-50 text-indigo-800 hover:bg-indigo-100 rounded-full font-medium border border-indigo-200"
        >
          🐛 Cotton Pest IPM
        </button>
        <button
          type="button"
          disabled={isStreaming}
          onClick={() => sendMessage('What is the current mandi bhav for soybean and wheat?')}
          className="text-xs px-3 py-1.5 bg-amber-50 text-amber-800 hover:bg-amber-100 rounded-full font-medium border border-amber-200"
        >
          💰 Mandi APMC Bhav
        </button>
        <button
          type="button"
          disabled={isStreaming}
          onClick={() => sendMessage('My cow has high fever, excessive saliva drooling and foot blisters')}
          className="text-xs px-3 py-1.5 bg-rose-50 text-rose-800 hover:bg-rose-100 rounded-full font-medium border border-rose-200"
        >
          🚨 Cow Saliva & Blisters (FMD)
        </button>
        <button
          type="button"
          disabled={isStreaming}
          onClick={() => sendMessage('Write python code to build a game')}
          className="text-xs px-3 py-1.5 bg-gray-100 text-gray-600 hover:bg-gray-200 rounded-full font-medium border border-gray-300"
          title="Test Domain Guardrail Refusal"
        >
          🚫 Test Off-Topic Guardrail
        </button>
      </div>

      {/* Main Chat Interface */}
      <div className="bg-white rounded-3xl shadow-xl border border-gray-200 flex flex-col h-[580px] overflow-hidden">
        {/* Messages Stream Container */}
        <div className="flex-1 overflow-y-auto p-6 space-y-5 bg-gray-50/40">
          {messages.map((m) => (
            <div
              key={m.id}
              className={`flex ${m.sender === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              <div
                className={`max-w-2xl rounded-3xl p-5 text-sm leading-relaxed shadow-sm transition-all ${
                  m.sender === 'user'
                    ? 'bg-emerald-600 text-white rounded-br-none'
                    : 'bg-white text-gray-900 rounded-bl-none border border-gray-200'
                }`}
              >
                {/* Header for AI responses with Read Aloud */}
                {m.sender === 'ai' && (
                  <div className="flex justify-between items-center pb-2 mb-2 border-b border-gray-100">
                    <div className="flex items-center gap-2 text-xs font-bold text-emerald-700 flex-wrap">
                      <span>🌾</span> FarmVet AI Agronomist
                      <span className="text-[10px] bg-emerald-50 text-emerald-800 font-semibold px-2 py-0.5 rounded-full border border-emerald-100">
                        {m.providerUsed || 'Agro-AI'}
                      </span>
                      {m.cacheHit && (
                        <span className="text-[10px] bg-amber-50 text-amber-700 font-bold px-2 py-0.5 rounded-full border border-amber-200">
                          ⚡ Cache ({m.latencyMs || '<5'}ms)
                        </span>
                      )}
                    </div>
                    {m.text && (
                      <button
                        type="button"
                        onClick={() => readAloud(m.text)}
                        className="text-[11px] text-gray-500 hover:text-emerald-700 flex items-center gap-1 px-2 py-0.5 rounded bg-gray-100 hover:bg-gray-200 transition-colors"
                        title="Listen to audio reading"
                      >
                        <span>🔊</span> Read Aloud
                      </button>
                    )}
                  </div>
                )}

                {/* Regulatory Safety Notice Banner */}
                {m.safetyNotice && (
                  <div className="mb-3 p-3 rounded-2xl bg-amber-50 border border-amber-200 text-amber-900 text-xs flex items-start gap-2 font-medium">
                    <span className="text-base shrink-0">🛡️</span>
                    <div>
                      <strong>Safety Notification:</strong> {m.safetyNotice}
                    </div>
                  </div>
                )}

                {/* ReAct steps if available */}
                {m.steps && m.steps.length > 0 && (
                  <details className="mb-3 p-2.5 bg-emerald-50/70 rounded-2xl border border-emerald-200 text-xs text-emerald-950">
                    <summary className="font-bold cursor-pointer select-none">
                      🧠 ReAct Reasoning & Tool Steps ({m.steps.length})
                    </summary>
                    <div className="mt-2 space-y-2 pl-2 border-l-2 border-emerald-300">
                      {m.steps.map((st, sidx) => (
                        <div key={sidx} className="space-y-0.5">
                          <div className="font-bold text-emerald-950">
                            #{sidx + 1} {st.stepName}
                          </div>
                          <div className="text-[11px] text-emerald-800">
                            <strong>Action:</strong> <code>{st.action}</code>
                          </div>
                          <div className="text-[11px] text-emerald-700">
                            <strong>Result:</strong> {st.observation}
                          </div>
                        </div>
                      ))}
                    </div>
                  </details>
                )}

                {/* Message Body with Streaming Markdown Parser */}
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
                      <span className="text-gray-400 italic">Synthesizing agronomy intelligence...</span>
                    )}
                    {isStreaming && m.id === messages[messages.length - 1].id && (
                      <span className="inline-block w-2 h-4 ml-1 bg-emerald-600 animate-pulse align-middle"></span>
                    )}
                  </div>
                )}

                {/* Dynamic Next Actions & Suggestion Chips */}
                {m.sender === 'ai' && (
                  <div className="mt-4 pt-3 border-t border-gray-100">
                    <div className="text-xs font-bold text-gray-400 uppercase mb-2 flex items-center gap-1.5">
                      <span>⚡</span>
                      <span>Related Actions:</span>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      {[
                        '🌾 Wheat CRI Irrigation & Fertilizer',
                        '🐛 Cotton Pest Management IPM',
                        '💧 Calculate Soil Moisture Water Needs',
                        '💰 Live Mandi Bhav for Soybean & Wheat',
                        '🐄 Cattle Fever & Mastitis First Aid'
                      ].map((act, actIdx) => (
                        <button
                          key={actIdx}
                          type="button"
                          disabled={isStreaming}
                          onClick={() => sendMessage(act)}
                          className="text-xs px-3 py-1.5 bg-emerald-50/80 hover:bg-emerald-100 text-emerald-900 rounded-xl font-semibold border border-emerald-200 transition-colors shadow-xs"
                        >
                          {act} &rarr;
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                {/* Clinical Emergency Fallback Box */}
                {m.fallback && (
                  <div className="mt-4 p-4 bg-rose-50 border border-rose-200 rounded-2xl text-xs text-rose-900 font-medium">
                    <div className="flex items-center gap-2 text-rose-800 font-bold text-sm mb-1">
                      <span>🚨</span> Veterinary Emergency Escalation
                    </div>
                    <p>
                      Symptoms indicate a serious clinical pathogen requiring physical palpation and stethoscope assessment.
                    </p>
                    <div className="mt-3">
                      <a
                        href="/consultations"
                        className="inline-block px-3 py-1.5 bg-rose-600 hover:bg-rose-700 text-white font-bold rounded-xl shadow-sm text-xs"
                      >
                        Book Immediate Tele-Consultation with Doctor &rarr;
                      </a>
                    </div>
                  </div>
                )}
              </div>
            </div>
          ))}

          {/* Real-time Tool Execution Progress Indicator */}
          {activeToolSteps.length > 0 && (
            <div className="flex justify-start">
              <div className="bg-emerald-50/80 border border-emerald-200 text-emerald-900 rounded-2xl p-3 text-xs flex items-center gap-2">
                <span className="w-3 h-3 rounded-full border-2 border-emerald-600 border-t-transparent animate-spin"></span>
                <span>{activeToolSteps[activeToolSteps.length - 1]}</span>
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Input Bar */}
        <div className="p-4 border-t bg-white">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              sendMessage();
            }}
            className="flex items-center gap-2"
          >
            <button
              type="button"
              onClick={startVoiceInput}
              disabled={isStreaming}
              className={`p-3 rounded-2xl text-base transition-all ${
                isListening
                  ? 'bg-rose-600 text-white animate-pulse'
                  : 'bg-gray-100 hover:bg-gray-200 text-gray-700'
              }`}
              title="Voice Speech-to-Text"
            >
              🎤
            </button>

            <input
              type="text"
              placeholder="Ask about crops, soil, pest sprays, Mandi rates, cow/buffalo health..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              disabled={isStreaming}
              className="flex-1 px-4 py-3 border border-gray-300 rounded-2xl focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 text-sm outline-none"
            />

            <Button
              type="submit"
              variant="primary"
              size="md"
              loading={isStreaming}
              className="bg-emerald-600 hover:bg-emerald-700 text-white font-bold px-6 py-3 rounded-2xl shadow-md transition-all shrink-0"
            >
              {isStreaming ? 'Streaming...' : 'Send'}
            </Button>
          </form>
        </div>
      </div>

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
    </div>
  );
}
