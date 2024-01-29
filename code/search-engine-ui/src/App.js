import './App.css';
import Search from './components/Search';
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Home from './components/Home';


function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/search" element={<Search />} />
      </Routes>
    </Router>
  );
}

export default App;
