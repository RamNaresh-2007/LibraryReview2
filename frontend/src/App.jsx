import { useState } from 'react';
import './App.css';
import Home from './components/Home.jsx';
import Login from './components/Login.jsx';
import { isLoggedIn as checkLoggedIn } from './lib.js';

const App = () => {
  const [loggedIn, setLoggedIn] = useState(checkLoggedIn());

  const handleLoginSuccess = () => {
    setLoggedIn(true);
  };

  const handleLogout = () => {
    setLoggedIn(false);
  };

  if (loggedIn) {
    return <Home onLogout={handleLogout} />;
  }

  return <Login onLoginSuccess={handleLoginSuccess} />;
};

export default App;
