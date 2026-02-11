import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
    const { user, logout, isAdmin, isAuthenticated } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const isActive = (path) => location.pathname === path;

    if (!isAuthenticated()) return null;

    return (
        <nav className="navbar">
            <div className="navbar-brand">
                <Link to="/">🚗 RentCar</Link>
            </div>
            <div className="navbar-menu">
                {isAdmin() ? (
                    <>
                        <Link to="/admin/cars" className={isActive('/admin/cars') ? 'active' : ''}>Kelola Mobil</Link>
                        <Link to="/admin/users" className={isActive('/admin/users') ? 'active' : ''}>Kelola User</Link>
                        <Link to="/admin/rentals" className={isActive('/admin/rentals') ? 'active' : ''}>Kelola Rental</Link>
                        <Link to="/admin/logs" className={isActive('/admin/logs') ? 'active' : ''}>Activity Log</Link>
                    </>
                ) : (
                    <>
                        <Link to="/cars" className={isActive('/cars') ? 'active' : ''}>Mobil</Link>
                        <Link to="/my-rentals" className={isActive('/my-rentals') ? 'active' : ''}>Rental Saya</Link>
                    </>
                )}
            </div>
            <div className="navbar-user">
                <span className="user-info">
                    <span className="user-badge">{user?.role}</span>
                    {user?.fullName}
                </span>
                <button onClick={handleLogout} className="btn btn-logout">Logout</button>
            </div>
        </nav>
    );
}
