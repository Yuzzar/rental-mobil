import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import CarCatalog from './pages/user/CarCatalog';
import MyRentals from './pages/user/MyRentals';
import CarManagement from './pages/admin/CarManagement';
import UserManagement from './pages/admin/UserManagement';
import RentalManagement from './pages/admin/RentalManagement';
import ActivityLog from './pages/admin/ActivityLog';

function AppRoutes() {
  const { isAuthenticated, isAdmin } = useAuth();

  return (
    <>
      <Navbar />
      <main className="main-content">
        <Routes>
          {/* Public */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* User */}
          <Route path="/cars" element={
            <ProtectedRoute><CarCatalog /></ProtectedRoute>
          } />
          <Route path="/my-rentals" element={
            <ProtectedRoute><MyRentals /></ProtectedRoute>
          } />

          {/* Admin */}
          <Route path="/admin/cars" element={
            <ProtectedRoute adminOnly><CarManagement /></ProtectedRoute>
          } />
          <Route path="/admin/users" element={
            <ProtectedRoute adminOnly><UserManagement /></ProtectedRoute>
          } />
          <Route path="/admin/rentals" element={
            <ProtectedRoute adminOnly><RentalManagement /></ProtectedRoute>
          } />
          <Route path="/admin/logs" element={
            <ProtectedRoute adminOnly><ActivityLog /></ProtectedRoute>
          } />

          {/* Default redirect */}
          <Route path="/" element={
            isAuthenticated() ? (
              isAdmin() ? <Navigate to="/admin/cars" /> : <Navigate to="/cars" />
            ) : (
              <Navigate to="/login" />
            )
          } />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </main>
    </>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <Router>
        <AppRoutes />
      </Router>
    </AuthProvider>
  );
}
