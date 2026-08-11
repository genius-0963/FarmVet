import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <div>
      <h2>FarmVet Connect</h2>
      <p>Welcome to FarmVet. Use the menu to register, login, and explore the backend API.</p>
      <ul>
        <li><Link to="/register">Register</Link></li>
        <li><Link to="/login">Login</Link></li>
        <li><Link to="/crops">Crops</Link></li>
        <li><Link to="/pesticides">Pesticides</Link></li>
        <li><Link to="/veterinarians">Veterinarians</Link></li>
      </ul>
    </div>
  );
}
