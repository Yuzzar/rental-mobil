import { useState, useEffect } from 'react';
import { userAPI } from '../../api/api';

export default function UserManagement() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editingUser, setEditingUser] = useState(null);
    const [form, setForm] = useState({
        username: '', password: '', fullName: '', email: '', phone: '', role: 'USER', active: true
    });
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => { fetchUsers(); }, []);

    const fetchUsers = async () => {
        try {
            const res = await userAPI.getAll();
            setUsers(res.data.data);
        } catch (err) { setError('Gagal memuat data user'); }
        finally { setLoading(false); }
    };

    const resetForm = () => {
        setForm({ username: '', password: '', fullName: '', email: '', phone: '', role: 'USER', active: true });
        setEditingUser(null);
        setError('');
    };

    const openCreate = () => { resetForm(); setShowModal(true); };

    const openEdit = (user) => {
        setForm({
            username: user.username, password: '', fullName: user.fullName,
            email: user.email, phone: user.phone || '', role: user.role, active: user.active
        });
        setEditingUser(user);
        setShowModal(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            const payload = { ...form };
            if (editingUser && !payload.password) delete payload.password;

            if (editingUser) {
                await userAPI.update(editingUser.id, payload);
                setSuccess('User berhasil diupdate');
            } else {
                await userAPI.create(payload);
                setSuccess('User berhasil dibuat');
            }
            setShowModal(false);
            fetchUsers();
            setTimeout(() => setSuccess(''), 3000);
        } catch (err) {
            setError(err.response?.data?.message || 'Gagal menyimpan data');
        }
    };

    const handleDelete = async (id) => {
        if (!confirm('Yakin ingin menghapus (deactivate) user ini?')) return;
        try {
            await userAPI.delete(id);
            setSuccess('User berhasil dihapus');
            fetchUsers();
            setTimeout(() => setSuccess(''), 3000);
        } catch (err) {
            setError(err.response?.data?.message || 'Gagal menghapus');
        }
    };

    if (loading) return <div className="loading">Loading...</div>;

    return (
        <div className="page">
            <div className="page-header">
                <h1>👥 Kelola User</h1>
                <button className="btn btn-primary" onClick={openCreate}>+ Tambah User</button>
            </div>

            {success && <div className="alert alert-success">{success}</div>}
            {error && !showModal && <div className="alert alert-error">{error}</div>}

            <div className="table-container glass">
                <table>
                    <thead>
                        <tr>
                            <th>ID</th><th>Username</th><th>Nama Lengkap</th>
                            <th>Email</th><th>Phone</th><th>Role</th><th>Status</th><th>Aksi</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map(user => (
                            <tr key={user.id}>
                                <td>{user.id}</td>
                                <td><strong>{user.username}</strong></td>
                                <td>{user.fullName}</td>
                                <td>{user.email}</td>
                                <td>{user.phone || '-'}</td>
                                <td><span className={`badge ${user.role === 'ADMIN' ? 'badge-info' : 'badge-default'}`}>{user.role}</span></td>
                                <td><span className={`badge ${user.active ? 'badge-success' : 'badge-danger'}`}>{user.active ? 'Active' : 'Inactive'}</span></td>
                                <td>
                                    <div className="action-btns">
                                        <button className="btn btn-sm btn-warning" onClick={() => openEdit(user)}>Edit</button>
                                        <button className="btn btn-sm btn-danger" onClick={() => handleDelete(user.id)}>Hapus</button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {users.length === 0 && <p className="empty-state">Belum ada data user</p>}
            </div>

            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal glass" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>{editingUser ? 'Edit User' : 'Tambah User'}</h2>
                            <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
                        </div>
                        {error && <div className="alert alert-error">{error}</div>}
                        <form onSubmit={handleSubmit}>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Username</label>
                                    <input type="text" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })}
                                        disabled={!!editingUser} required />
                                </div>
                                <div className="form-group">
                                    <label>Password {editingUser && '(kosongkan jika tidak diubah)'}</label>
                                    <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })}
                                        required={!editingUser} />
                                </div>
                            </div>
                            <div className="form-group">
                                <label>Nama Lengkap</label>
                                <input type="text" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} required />
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Email</label>
                                    <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
                                </div>
                                <div className="form-group">
                                    <label>Phone</label>
                                    <input type="text" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
                                </div>
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Role</label>
                                    <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
                                        <option value="USER">USER</option>
                                        <option value="ADMIN">ADMIN</option>
                                    </select>
                                </div>
                                {editingUser && (
                                    <div className="form-group">
                                        <label>Status</label>
                                        <select value={form.active} onChange={(e) => setForm({ ...form, active: e.target.value === 'true' })}>
                                            <option value="true">Active</option>
                                            <option value="false">Inactive</option>
                                        </select>
                                    </div>
                                )}
                            </div>
                            <div className="form-actions">
                                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Batal</button>
                                <button type="submit" className="btn btn-primary">{editingUser ? 'Update' : 'Simpan'}</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
