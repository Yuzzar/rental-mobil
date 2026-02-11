import { useState, useEffect } from 'react';
import { rentalAPI } from '../../api/api';

export default function MyRentals() {
    const [rentals, setRentals] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => { fetchRentals(); }, []);

    const fetchRentals = async () => {
        try {
            const res = await rentalAPI.getMyRentals();
            setRentals(res.data.data);
        } catch (err) { setError('Gagal memuat data rental'); }
        finally { setLoading(false); }
    };

    const handleCancel = async (id) => {
        if (!confirm('Yakin ingin membatalkan rental ini?')) return;
        try {
            await rentalAPI.cancel(id);
            setSuccess('Rental berhasil dibatalkan');
            fetchRentals();
            setTimeout(() => setSuccess(''), 3000);
        } catch (err) {
            setError(err.response?.data?.message || 'Gagal membatalkan');
        }
    };

    const statusBadge = (status) => {
        const colors = {
            PENDING: 'badge-warning', APPROVED: 'badge-info', ACTIVE: 'badge-primary',
            COMPLETED: 'badge-success', CANCELLED: 'badge-secondary', REJECTED: 'badge-danger'
        };
        const labels = {
            PENDING: '⏳ Menunggu', APPROVED: '✅ Disetujui', ACTIVE: '🚗 Aktif',
            COMPLETED: '✔️ Selesai', CANCELLED: '❌ Dibatalkan', REJECTED: '🚫 Ditolak'
        };
        return <span className={`badge ${colors[status] || ''}`}>{labels[status] || status}</span>;
    };

    if (loading) return <div className="loading">Loading...</div>;

    return (
        <div className="page">
            <div className="page-header">
                <h1>📋 Rental Saya</h1>
            </div>

            {success && <div className="alert alert-success">{success}</div>}
            {error && <div className="alert alert-error">{error}</div>}

            <div className="rental-cards">
                {rentals.map(r => (
                    <div key={r.id} className="rental-card glass">
                        <div className="rental-card-header">
                            <div>
                                <h3>{r.carBrand} {r.carModel}</h3>
                                <p className="text-muted">{r.carLicensePlate}</p>
                            </div>
                            {statusBadge(r.status)}
                        </div>
                        <div className="rental-card-body">
                            <div className="rental-dates">
                                <div>
                                    <small>Mulai</small>
                                    <strong>{r.startDate}</strong>
                                </div>
                                <div className="rental-arrow">→</div>
                                <div>
                                    <small>Selesai</small>
                                    <strong>{r.endDate}</strong>
                                </div>
                            </div>
                            <div className="rental-cost">
                                <small>Total Biaya</small>
                                <strong>Rp {Number(r.totalCost).toLocaleString('id-ID')}</strong>
                            </div>
                        </div>
                        {r.status === 'PENDING' && (
                            <div className="rental-card-footer">
                                <button className="btn btn-danger btn-sm" onClick={() => handleCancel(r.id)}>
                                    Batalkan
                                </button>
                            </div>
                        )}
                    </div>
                ))}
            </div>

            {rentals.length === 0 && (
                <div className="empty-state-large">
                    <span>📭</span>
                    <p>Belum ada riwayat rental</p>
                </div>
            )}
        </div>
    );
}
