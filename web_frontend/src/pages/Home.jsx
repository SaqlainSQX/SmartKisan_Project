import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Cloud, Thermometer, Wind, Droplets } from 'lucide-react';
import api from '../services/api';

const Home = () => {
  const { user } = useAuth();
  const [weather, setWeather] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // In a real app, you'd get the user's location via geolocation API
    // For this prototype, we'll fetch a default location or a mock weather from backend
    // Since backend has weather routes according to Kotlin, let's assume there's a route
    // If not, we'll just show some mock data for the UI
    const fetchWeather = async () => {
      try {
        // Mock data to ensure the UI looks premium even if API is missing
        setTimeout(() => {
          setWeather({
            temp: 28,
            condition: 'Partly Cloudy',
            humidity: 65,
            windSpeed: 12,
            location: 'Local Farm Area'
          });
          setLoading(false);
        }, 1000);
      } catch (err) {
        setLoading(false);
      }
    };

    fetchWeather();
  }, []);

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Welcome back, <span style={{ color: 'var(--primary-light)' }}>Farmer</span></h1>
      
      <div className="card glass-panel" style={{ marginBottom: '2rem' }}>
        <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem' }}>
          <Cloud color="var(--secondary-color)" /> Weather Update
        </h2>
        
        {loading ? (
          <div>Loading weather data...</div>
        ) : weather ? (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
            <div style={{ background: 'rgba(255,255,255,0.05)', padding: '1.5rem', borderRadius: '12px', textAlign: 'center' }}>
              <div style={{ fontSize: '3rem', fontWeight: 'bold', color: 'white' }}>{weather.temp}°C</div>
              <div style={{ color: 'var(--text-muted)' }}>{weather.condition}</div>
            </div>
            
            <div style={{ background: 'rgba(255,255,255,0.05)', padding: '1.5rem', borderRadius: '12px', display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <Thermometer color="var(--primary-light)" />
                <span>Feels like {weather.temp + 2}°C</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <Droplets color="var(--primary-light)" />
                <span>Humidity: {weather.humidity}%</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <Wind color="var(--primary-light)" />
                <span>Wind: {weather.windSpeed} km/h</span>
              </div>
            </div>
          </div>
        ) : (
          <div>Failed to load weather data.</div>
        )}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem' }}>
         <div className="card" style={{ borderLeft: '4px solid var(--primary-color)' }}>
           <h3>Recent Activity</h3>
           <p style={{ color: 'var(--text-muted)', marginTop: '1rem' }}>No recent activities found. Start by detecting a crop disease.</p>
         </div>
         <div className="card" style={{ borderLeft: '4px solid var(--secondary-color)' }}>
           <h3>Quick Tips</h3>
           <ul style={{ color: 'var(--text-muted)', marginTop: '1rem', paddingLeft: '1.5rem' }}>
             <li style={{ marginBottom: '0.5rem' }}>Water your crops early in the morning to reduce evaporation.</li>
             <li style={{ marginBottom: '0.5rem' }}>Monitor soil moisture regularly.</li>
             <li>Use the Disease Detection tool at the first sign of leaf discoloration.</li>
           </ul>
         </div>
      </div>
    </div>
  );
};

export default Home;
