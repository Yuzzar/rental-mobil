import { useState, useEffect } from 'react';
import { rentalAPI } from '../../api/api';

export default function RentalManagement() {
    const [rentals, setRentals] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => { fetchRentals(); }, []);

    const fetchRentals = async () => {
        try {
            const res = await rentalAPI.getAll();
            setRentals(res.data.data);
        } catch (err) { setError('Gagal memuat data rental'); }
        finally { setLoading(false); }
    };

    const handleAction = async (id, action, label) => {
        try {
            setError('');
            if (action === 'approve') await rentalAPI.approve(id);
            else if (action === 'reject') await rentalAPI.reject(id);
            else if (action === 'complete') await rentalAPI.complete(id);

            setSuccess(`Rental berhasil di-${label}`);
            fetchRentals();
            setTimeout(() => setSuccess(''), 3000);
        } catch (err) {
            setError(err.response?.data?.message || 'Gagal memproses');
        }
    };

    const statusBadge = (status) => {
        const colors = {
            PENDING: 'badge-warning', APPROVED: 'badge-info', ACTIVE: 'badge-primary',
            COMPLETED: 'badge-success', CANCELLED: 'badge-secondary', REJECTED: 'badge-danger'
        };
        return <span className={`badge ${colors[status] || ''}`}>{status}</span>;
    };

    if (loading) return <div className="loading">Loading...</div>;

    return (
        <div className="page">
            <div className="page-header">
                <h1>📋 Kelola Rental</h1>
            </div>

            {success && <div className="alert alert-success">{success}</div>}
            {error && <div className="alert alert-error">{error}</div>}

            <div className="table-container glass">
                <table>
                    <thead>
                        <tr>
                            <th>ID</th><th>User</th><th>Mobil</th><th>Plat</th>
                            <th>Mulai</th><th>Selesai</th><th>Total</th><th>Status</th><th>Aksi</th>
                        </tr>
                    </thead>
                    <tbody>
                        {rentals.map(r => (
                            <tr key={r.id}>
                                <td>{r.id}</td>
                                <td>
                                    <div><strong>{r.userFullName}</strong></div>
                                    <small className="text-muted">@{r.userName}</small>
                                </td>
                                <td><strong>{r.carBrand} {r.carModel}</strong></td>
                                <td><code>{r.carLicensePlate}</code></td>
                                <td>{r.startDate}</td>
                                <td>{r.endDate}</td>
                                <td>Rp {Number(r.totalCost).toLocaleString('id-ID')}</td>
                                <td>{statusBadge(r.status)}</td>
                                <td>
                                    <div className="action-btns">
                                        {r.status === 'PENDING' && (
                                            <>
                                                <button className="btn btn-sm btn-success" onClick={() => handleAction(r.id, 'approve', 'approve')}>Approve</button>
                                                <button className="btn btn-sm btn-danger" onClick={() => handleAction(r.id, 'reject', 'reject')}>Reject</button>
                                            </>
                                        )}
                                        {(r.status === 'APPROVED' || r.status === 'ACTIVE') && (
                                            <button className="btn btn-sm btn-info" onClick={() => handleAction(r.id, 'complete', 'selesaikan')}>Selesai</button>
                                        )}
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {rentals.length === 0 && <p className="empty-state">Belum ada data rental</p>}
            </div>
        </div>
    );
}
