import React, { useState, useRef, useEffect } from 'react';
import { Send, Bot, User } from 'lucide-react';
import api from '../services/api';

const Chatbot = () => {
  const [messages, setMessages] = useState([
    { id: 1, text: "Hello! I'm your Agri-Assistant. How can I help you with your farming today?", sender: 'bot' }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim()) return;
    
    const userMsg = input.trim();
    setInput('');
    
    setMessages(prev => [...prev, { id: Date.now(), text: userMsg, sender: 'user' }]);
    setLoading(true);
    
    try {
      const response = await api.post('/chatbot/ask', { query: userMsg });
      setMessages(prev => [...prev, { id: Date.now(), text: response.data.reply || response.data.answer || "I'm sorry, I couldn't process that.", sender: 'bot' }]);
    } catch (error) {
      setMessages(prev => [...prev, { id: Date.now(), text: "Sorry, I'm having trouble connecting to the server right now. Please try again later.", sender: 'bot' }]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSend();
    }
  };

  return (
    <div style={{ height: 'calc(100vh - 120px)', display: 'flex', flexDirection: 'column' }}>
      <div style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        <Bot color="var(--primary-light)" size={32} />
        <div>
          <h2 style={{ margin: 0 }}>Agri-Assistant</h2>
          <p style={{ margin: 0, color: 'var(--text-muted)', fontSize: '0.9rem' }}>AI-powered farming advice</p>
        </div>
      </div>

      <div className="card glass-panel chat-container" style={{ flex: 1, padding: 0, overflow: 'hidden' }}>
        <div className="chat-messages" style={{ height: '100%', overflowY: 'auto' }}>
          {messages.map((msg) => (
            <div key={msg.id} style={{ display: 'flex', flexDirection: msg.sender === 'user' ? 'row-reverse' : 'row', gap: '0.75rem', marginBottom: '1rem' }}>
              <div style={{ 
                width: '36px', height: '36px', borderRadius: '50%', 
                background: msg.sender === 'user' ? 'rgba(255,255,255,0.1)' : 'rgba(46, 125, 50, 0.2)',
                display: 'flex', justifyContent: 'center', alignItems: 'center'
              }}>
                {msg.sender === 'user' ? <User size={18} /> : <Bot size={18} color="var(--primary-light)" />}
              </div>
              <div className={`message ${msg.sender}`}>
                <div 
                  style={{ whiteSpace: 'pre-wrap' }}
                  dangerouslySetInnerHTML={{ __html: msg.text.replace(/\n/g, '<br/>') }}
                />
              </div>
            </div>
          ))}
          {loading && (
            <div style={{ display: 'flex', flexDirection: 'row', gap: '0.75rem', marginBottom: '1rem' }}>
              <div style={{ width: '36px', height: '36px', borderRadius: '50%', background: 'rgba(46, 125, 50, 0.2)', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                <Bot size={18} color="var(--primary-light)" />
              </div>
              <div className="message bot" style={{ display: 'flex', gap: '0.25rem', alignItems: 'center', height: '40px' }}>
                <div style={{ width: '6px', height: '6px', background: 'var(--text-muted)', borderRadius: '50%', animation: 'bounce 1.4s infinite ease-in-out both' }}></div>
                <div style={{ width: '6px', height: '6px', background: 'var(--text-muted)', borderRadius: '50%', animation: 'bounce 1.4s infinite ease-in-out both', animationDelay: '0.2s' }}></div>
                <div style={{ width: '6px', height: '6px', background: 'var(--text-muted)', borderRadius: '50%', animation: 'bounce 1.4s infinite ease-in-out both', animationDelay: '0.4s' }}></div>
                <style>{`@keyframes bounce { 0%, 80%, 100% { transform: scale(0); } 40% { transform: scale(1); } }`}</style>
              </div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        <div className="chat-input-area" style={{ background: 'rgba(0,0,0,0.2)' }}>
          <input 
            type="text" 
            className="input-field" 
            style={{ flex: 1, background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)' }}
            placeholder="Ask about crop diseases, farming techniques..." 
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyPress={handleKeyPress}
          />
          <button 
            className="btn-primary" 
            style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '12px', borderRadius: '8px' }}
            onClick={handleSend}
            disabled={loading || !input.trim()}
          >
            <Send size={20} />
          </button>
        </div>
      </div>
    </div>
  );
};

export default Chatbot;
