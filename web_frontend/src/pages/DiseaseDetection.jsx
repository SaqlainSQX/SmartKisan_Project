import React, { useState, useRef } from 'react';
import { UploadCloud, CheckCircle, AlertTriangle, RefreshCw } from 'lucide-react';
import api from '../services/api';

const DiseaseDetection = () => {
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      setFile(selectedFile);
      setPreview(URL.createObjectURL(selectedFile));
      setResult(null);
      setError(null);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const droppedFile = e.dataTransfer.files[0];
      setFile(droppedFile);
      setPreview(URL.createObjectURL(droppedFile));
      setResult(null);
      setError(null);
    }
  };

  const handleUpload = async () => {
    if (!file) return;
    
    setLoading(true);
    setError(null);
    
    const formData = new FormData();
    formData.append('file', file);
    
    try {
      const response = await api.post('/disease/predict', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: 60000 // Disease prediction takes time due to Gemini
      });
      setResult(response.data);
    } catch (err) {
      setError(err.response?.data?.detail || 'An error occurred during prediction.');
    } finally {
      setLoading(false);
    }
  };

  const reset = () => {
    setFile(null);
    setPreview(null);
    setResult(null);
    setError(null);
  };

  return (
    <div>
      <h1 style={{ marginBottom: '1rem' }}>Crop Disease Detection</h1>
      <p style={{ color: 'var(--text-muted)', marginBottom: '2rem' }}>
        Upload a clear image of the affected plant leaf to receive an AI-powered diagnosis and treatment plan.
      </p>

      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '2rem' }}>
        <div style={{ flex: '1 1 400px' }}>
          <div className="card glass-panel" style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
            {!preview ? (
              <div 
                className="upload-area" 
                onClick={() => fileInputRef.current?.click()}
                onDragOver={handleDragOver}
                onDrop={handleDrop}
                style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}
              >
                <UploadCloud className="upload-icon" />
                <h3 style={{ marginBottom: '0.5rem' }}>Drag & Drop your image here</h3>
                <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>or click to browse from your computer</p>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Supports JPG, PNG, JPEG</div>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                <div style={{ flex: 1, borderRadius: '12px', overflow: 'hidden', marginBottom: '1.5rem', background: '#000', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                  <img src={preview} alt="Leaf Preview" style={{ maxWidth: '100%', maxHeight: '300px', objectFit: 'contain' }} />
                </div>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <button onClick={reset} className="btn-primary" style={{ flex: 1, background: 'rgba(255,255,255,0.1)', color: 'white', border: '1px solid var(--glass-border)', boxShadow: 'none' }}>
                    <RefreshCw size={18} style={{ marginRight: '0.5rem', verticalAlign: 'middle' }} /> Change
                  </button>
                  <button onClick={handleUpload} className="btn-primary" style={{ flex: 2 }} disabled={loading}>
                    {loading ? 'Analyzing...' : 'Predict Disease'}
                  </button>
                </div>
              </div>
            )}
            <input 
              type="file" 
              ref={fileInputRef} 
              style={{ display: 'none' }} 
              accept="image/*"
              onChange={handleFileChange} 
            />
          </div>
        </div>

        {/* Results Area */}
        <div style={{ flex: '1 1 400px' }}>
          {loading && (
             <div className="card glass-panel" style={{ height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
               <div style={{ width: '50px', height: '50px', border: '4px solid rgba(255,255,255,0.1)', borderTop: '4px solid var(--primary-light)', borderRadius: '50%', animation: 'spin 1s linear infinite' }}></div>
               <h3 style={{ marginTop: '1.5rem' }}>AI is analyzing the leaf...</h3>
               <p style={{ color: 'var(--text-muted)', textAlign: 'center', marginTop: '0.5rem' }}>This may take a few moments as we consult the Gemini AI for detailed treatment solutions.</p>
               <style>{`@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }`}</style>
             </div>
          )}

          {error && (
            <div className="card" style={{ borderLeft: '4px solid #ef5350' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#ef5350', marginBottom: '1rem' }}>
                <AlertTriangle />
                <h3 style={{ margin: 0 }}>Analysis Failed</h3>
              </div>
              <p>{error}</p>
            </div>
          )}

          {result && (
            <div className="card glass-panel animate-fade-in" style={{ height: '100%', borderTop: '4px solid var(--primary-color)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: 'var(--primary-light)', marginBottom: '1.5rem' }}>
                <CheckCircle />
                <h2 style={{ margin: 0 }}>Diagnosis Complete</h2>
              </div>
              
              <div style={{ marginBottom: '1.5rem' }}>
                <div style={{ fontSize: '0.9rem', color: 'var(--text-muted)', marginBottom: '0.25rem' }}>Detected Condition</div>
                <h3 style={{ color: 'white', fontSize: '1.5rem' }}>{result.disease_name || result.prediction || 'Unknown'}</h3>
              </div>

              {result.gemini_info && (
                <div style={{ padding: '1.5rem', background: 'rgba(255,255,255,0.05)', borderRadius: '12px' }}>
                  <h4 style={{ marginBottom: '1rem', color: 'var(--secondary-color)' }}>Treatment & Recommendations</h4>
                  <div 
                    style={{ lineHeight: '1.6', color: '#e0e0e0', whiteSpace: 'pre-wrap' }}
                    dangerouslySetInnerHTML={{ __html: result.gemini_info.replace(/\n/g, '<br/>') }}
                  />
                </div>
              )}
            </div>
          )}

          {!loading && !result && !error && (
            <div className="card glass-panel" style={{ height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', opacity: 0.5 }}>
              <CheckCircle size={48} style={{ marginBottom: '1rem', color: 'var(--text-muted)' }} />
              <h3>Results will appear here</h3>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DiseaseDetection;
