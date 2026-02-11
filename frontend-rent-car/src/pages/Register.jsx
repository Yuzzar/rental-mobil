import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI } from '../api/api';

export default function Register() {
    const [form, setForm] = useState({
        username: '', password: '', fullName: '', email: '', phone: ''
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            await authAPI.register(form);
            navigate('/login', { state: { message: 'Registrasi berhasil! Silakan login.' } });
        } catch (err) {
            setError(err.response?.data?.message || 'Registrasi gagal');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-card glass">
                <div className="auth-header">
                    <h1>🚗 RentCar</h1>
                    <p>Buat akun baru</p>
                </div>
                {error && <div className="alert alert-error">{error}</div>}
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Nama Lengkap</label>
                        <input type="text" value={form.fullName}
                            onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                            placeholder="Masukkan nama lengkap" required />
                    </div>
                    <div className="form-group">
                        <label>Username</label>
                        <input type="text" value={form.username}
                            onChange={(e) => setForm({ ...form, username: e.target.value })}
                            placeholder="Masukkan username" required />
                    </div>
                    <div className="form-group">
                        <label>Email</label>
                        <input type="email" value={form.email}
                            onChange={(e) => setForm({ ...form, email: e.target.value })}
                            placeholder="Masukkan email" required />
                    </div>
                    <div className="form-group">
                        <label>No. Telepon</label>
                        <input type="text" value={form.phone}
                            onChange={(e) => setForm({ ...form, phone: e.target.value })}
                            placeholder="Masukkan no. telepon" />
                    </div>
                    <div className="form-group">
                        <label>Password</label>
                        <input type="password" value={form.password}
                            onChange={(e) => setForm({ ...form, password: e.target.value })}
                            placeholder="Minimal 6 karakter" required />
                    </div>
                    <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
                        {loading ? 'Loading...' : 'Daftar'}
                    </button>
                </form>
                <p className="auth-footer">
                    Sudah punya akun? <Link to="/login">Login di sini</Link>
                </p>
            </div>
        </div>
    );
}
