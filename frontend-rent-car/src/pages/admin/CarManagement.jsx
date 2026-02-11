import { useState, useEffect } from 'react';
import { carAPI } from '../../api/api';

export default function CarManagement() {
    const [cars, setCars] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editingCar, setEditingCar] = useState(null);
    const [form, setForm] = useState({
        brand: '', model: '', year: '', licensePlate: '',
        color: '', dailyRate: '', status: 'AVAILABLE', imageUrl: ''
    });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => { fetchCars(); }, []);

    const fetchCars = async () => {
        try {
            const res = await carAPI.getAll();
            setCars(res.data.data);
        } catch (err) { setError('Gagal memuat data mobil'); }
        finally { setLoading(false); }
    };

    const resetForm = () => {
        setForm({ brand: '', model: '', year: '', licensePlate: '', color: '', dailyRate: '', status: 'AVAILABLE', imageUrl: '' });
        setEditingCar(null);
        setError('');
    };

    const openCreate = () => { resetForm(); setShowModal(true); };

    const openEdit = (car) => {
        setForm({
            brand: car.brand, model: car.model, year: car.year,
            licensePlate: car.licensePlate, color: car.color,
            dailyRate: car.dailyRate, status: car.status, imageUrl: car.imageUrl || ''
        });
        setEditingCar(car);
        setShowModal(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            if (editingCar) {
                await carAPI.update(editingCar.id, { ...form, year: parseInt(form.year), dailyRate: parseFloat(form.dailyRate) });
                setSuccess('Mobil berhasil diupdate');
            } else {
                await carAPI.create({ ...form, year: parseInt(form.year), dailyRate: parseFloat(form.dailyRate) });
                setSuccess('Mobil berhasil ditambahkan');
            }
            setShowModal(false);
            fetchCars();
            setTimeout(() => setSuccess(''), 3000);
        } catch (err) {
            setError(err.response?.data?.message || 'Gagal menyimpan data');
        }
    };

    const handleDelete = async (id) => {
        if (!confirm('Yakin ingin menghapus mobil ini?')) return;
        try {
            await carAPI.delete(id);
            setSuccess('Mobil berhasil dihapus');
            fetchCars();
            setTimeout(() => setSuccess(''), 3000);
        } catch (err) {
            setError(err.response?.data?.message || 'Gagal menghapus');
        }
    };

    const statusBadge = (status) => {
        const colors = { AVAILABLE: 'badge-success', RENTED: 'badge-warning', MAINTENANCE: 'badge-danger' };
        return <span className={`badge ${colors[status] || ''}`}>{status}</span>;
    };

    if (loading) return <div className="loading">Loading...</div>;

    return (
        <div className="page">
            <div className="page-header">
                <h1>🚗 Kelola Mobil</h1>
                <button className="btn btn-primary" onClick={openCreate}>+ Tambah Mobil</button>
            </div>

            {success && <div className="alert alert-success">{success}</div>}
            {error && !showModal && <div className="alert alert-error">{error}</div>}

            <div className="table-container glass">
                <table>
                    <thead>
                        <tr>
                            <th>ID</th><th>Brand</th><th>Model</th><th>Tahun</th>
                            <th>Plat Nomor</th><th>Warna</th><th>Harga/Hari</th>
                            <th>Status</th><th>Aksi</th>
                        </tr>
                    </thead>
                    <tbody>
                        {cars.map(car => (
                            <tr key={car.id}>
                                <td>{car.id}</td>
                                <td><strong>{car.brand}</strong></td>
                                <td>{car.model}</td>
                                <td>{car.year}</td>
                                <td><code>{car.licensePlate}</code></td>
                                <td>{car.color}</td>
                                <td>Rp {Number(car.dailyRate).toLocaleString('id-ID')}</td>
                                <td>{statusBadge(car.status)}</td>
                                <td>
                                    <div className="action-btns">
                                        <button className="btn btn-sm btn-warning" onClick={() => openEdit(car)}>Edit</button>
                                        <button className="btn btn-sm btn-danger" onClick={() => handleDelete(car.id)}>Hapus</button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {cars.length === 0 && <p className="empty-state">Belum ada data mobil</p>}
            </div>

            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal glass" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>{editingCar ? 'Edit Mobil' : 'Tambah Mobil'}</h2>
                            <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
                        </div>
                        {error && <div className="alert alert-error">{error}</div>}
                        <form onSubmit={handleSubmit}>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Brand</label>
                                    <input type="text" value={form.brand} onChange={(e) => setForm({ ...form, brand: e.target.value })} required />
                                </div>
                                <div className="form-group">
                                    <label>Model</label>
                                    <input type="text" value={form.model} onChange={(e) => setForm({ ...form, model: e.target.value })} required />
                                </div>
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Tahun</label>
                                    <input type="number" value={form.year} onChange={(e) => setForm({ ...form, year: e.target.value })} required />
                                </div>
                                <div className="form-group">
                                    <label>Plat Nomor</label>
                                    <input type="text" value={form.licensePlate} onChange={(e) => setForm({ ...form, licensePlate: e.target.value })} required />
                                </div>
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Warna</label>
                                    <input type="text" value={form.color} onChange={(e) => setForm({ ...form, color: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>Harga per Hari (Rp)</label>
                                    <input type="number" value={form.dailyRate} onChange={(e) => setForm({ ...form, dailyRate: e.target.value })} required />
                                </div>
                            </div>
                            {editingCar && (
                                <div className="form-group">
                                    <label>Status</label>
                                    <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                                        <option value="AVAILABLE">Available</option>
                                        <option value="RENTED">Rented</option>
                                        <option value="MAINTENANCE">Maintenance</option>
                                    </select>
                                </div>
                            )}
                            <div className="form-actions">
                                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Batal</button>
                                <button type="submit" className="btn btn-primary">{editingCar ? 'Update' : 'Simpan'}</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
