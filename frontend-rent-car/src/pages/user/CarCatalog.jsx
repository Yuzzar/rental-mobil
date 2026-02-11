import { useState, useEffect } from 'react';
import { carAPI, rentalAPI } from '../../api/api';
import { useAuth } from '../../context/AuthContext';

export default function CarCatalog() {
    const [cars, setCars] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState({ brand: '', status: '' });
    const [rentalModal, setRentalModal] = useState(null);
    const [rentalForm, setRentalForm] = useState({ startDate: '', endDate: '' });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const { isAuthenticated } = useAuth();

    useEffect(() => { fetchCars(); }, []);

    const fetchCars = async (params) => {
        try {
            const res = await carAPI.getAll(params);
            setCars(res.data.data);
        } catch (err) { setError('Gagal memuat data mobil'); }
        finally { setLoading(false); }
    };

    const handleFilter = () => {
        const params = {};
        if (filter.brand) params.brand = filter.brand;
        if (filter.status) params.status = filter.status;
        fetchCars(params);
    };

    const handleReset = () => {
        setFilter({ brand: '', status: '' });
        fetchCars();
    };

    const openRentalModal = (car) => {
        if (!isAuthenticated()) {
            window.location.href = '/login';
            return;
        }
        setRentalModal(car);
        setRentalForm({ startDate: '', endDate: '' });
        setError('');
    };

    const handleRental = async (e) => {
        e.preventDefault();
        setError('');
        try {
            await rentalAPI.create({
                carId: rentalModal.id,
                startDate: rentalForm.startDate,
                endDate: rentalForm.endDate,
            });
            setSuccess('Rental berhasil dibuat! Menunggu approval admin.');
            setRentalModal(null);
            fetchCars();
            setTimeout(() => setSuccess(''), 5000);
        } catch (err) {
            setError(err.response?.data?.message || 'Gagal membuat rental');
        }
    };

    const calculateCost = () => {
        if (!rentalForm.startDate || !rentalForm.endDate || !rentalModal) return 0;
        const start = new Date(rentalForm.startDate);
        const end = new Date(rentalForm.endDate);
        const days = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
        if (days < 1) return 0;
        return days * rentalModal.dailyRate;
    };

    const statusColor = (status) => {
        const colors = { AVAILABLE: '#4ade80', RENTED: '#fbbf24', MAINTENANCE: '#ef4444' };
        return colors[status] || '#94a3b8';
    };

    if (loading) return <div className="loading">Loading...</div>;

    return (
        <div className="page">
            <div className="page-header">
                <h1>🚗 Katalog Mobil</h1>
            </div>

            {success && <div className="alert alert-success">{success}</div>}

            <div className="filter-section glass">
                <div className="filter-row">
                    <div className="form-group">
                        <label>Brand</label>
                        <input type="text" placeholder="Cari brand..." value={filter.brand}
                            onChange={(e) => setFilter({ ...filter, brand: e.target.value })} />
                    </div>
                    <div className="form-group">
                        <label>Status</label>
                        <select value={filter.status} onChange={(e) => setFilter({ ...filter, status: e.target.value })}>
                            <option value="">Semua</option>
                            <option value="AVAILABLE">Available</option>
                            <option value="RENTED">Rented</option>
                            <option value="MAINTENANCE">Maintenance</option>
                        </select>
                    </div>
                    <div className="filter-actions">
                        <button className="btn btn-primary" onClick={handleFilter}>🔍 Filter</button>
                        <button className="btn btn-secondary" onClick={handleReset}>Reset</button>
                    </div>
                </div>
            </div>

            <div className="car-grid">
                {cars.map(car => (
                    <div key={car.id} className="car-card glass">
                        <div className="car-image">
                            <div className="car-placeholder">🚗</div>
                            <span className="car-status-badge" style={{ backgroundColor: statusColor(car.status) }}>
                                {car.status}
                            </span>
                        </div>
                        <div className="car-info">
                            <h3>{car.brand} {car.model}</h3>
                            <div className="car-details">
                                <span>📅 {car.year}</span>
                                <span>🎨 {car.color}</span>
                                <span>🔖 {car.licensePlate}</span>
                            </div>
                            <div className="car-price">
                                <span className="price">Rp {Number(car.dailyRate).toLocaleString('id-ID')}</span>
                                <span className="per-day">/ hari</span>
                            </div>
                            {car.status === 'AVAILABLE' && (
                                <button className="btn btn-primary btn-full" onClick={() => openRentalModal(car)}>
                                    Rental Sekarang
                                </button>
                            )}
                            {car.status !== 'AVAILABLE' && (
                                <button className="btn btn-disabled btn-full" disabled>
                                    {car.status === 'RENTED' ? 'Sedang Dirental' : 'Dalam Maintenance'}
                                </button>
                            )}
                        </div>
                    </div>
                ))}
            </div>

            {cars.length === 0 && (
                <div className="empty-state-large">
                    <span>🚫</span>
                    <p>Tidak ada mobil ditemukan</p>
                </div>
            )}

            {rentalModal && (
                <div className="modal-overlay" onClick={() => setRentalModal(null)}>
                    <div className="modal glass" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>Rental {rentalModal.brand} {rentalModal.model}</h2>
                            <button className="modal-close" onClick={() => setRentalModal(null)}>✕</button>
                        </div>
                        <div className="rental-summary">
                            <p>🔖 Plat: <strong>{rentalModal.licensePlate}</strong></p>
                            <p>💰 Harga: <strong>Rp {Number(rentalModal.dailyRate).toLocaleString('id-ID')}/hari</strong></p>
                        </div>
                        {error && <div className="alert alert-error">{error}</div>}
                        <form onSubmit={handleRental}>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Tanggal Mulai</label>
                                    <input type="date" value={rentalForm.startDate}
                                        min={new Date().toISOString().split('T')[0]}
                                        onChange={(e) => setRentalForm({ ...rentalForm, startDate: e.target.value })} required />
                                </div>
                                <div className="form-group">
                                    <label>Tanggal Selesai</label>
                                    <input type="date" value={rentalForm.endDate}
                                        min={rentalForm.startDate || new Date().toISOString().split('T')[0]}
                                        onChange={(e) => setRentalForm({ ...rentalForm, endDate: e.target.value })} required />
                                </div>
                            </div>
                            {calculateCost() > 0 && (
                                <div className="total-cost-preview">
                                    <span>Estimasi Total:</span>
                                    <span className="total-amount">Rp {calculateCost().toLocaleString('id-ID')}</span>
                                </div>
                            )}
                            <div className="form-actions">
                                <button type="button" className="btn btn-secondary" onClick={() => setRentalModal(null)}>Batal</button>
                                <button type="submit" className="btn btn-primary">Konfirmasi Rental</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
