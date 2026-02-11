import { useState, useEffect } from 'react';
import { logAPI } from '../../api/api';

export default function ActivityLog() {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('');

    useEffect(() => { fetchLogs(); }, []);

    const fetchLogs = async () => {
        try {
            const res = await logAPI.getAll();
            setLogs(res.data.data);
        } catch (err) { console.error(err); }
        finally { setLoading(false); }
    };

    const filteredLogs = logs.filter(log =>
        !filter ||
        log.username.toLowerCase().includes(filter.toLowerCase()) ||
        log.action.toLowerCase().includes(filter.toLowerCase()) ||
        log.entityName.toLowerCase().includes(filter.toLowerCase()) ||
        (log.details && log.details.toLowerCase().includes(filter.toLowerCase()))
    );

    const actionBadge = (action) => {
        if (action.includes('CREATE')) return 'badge-success';
        if (action.includes('UPDATE') || action.includes('APPROVE')) return 'badge-info';
        if (action.includes('DELETE') || action.includes('REJECT')) return 'badge-danger';
        if (action.includes('LOGIN') || action.includes('REGISTER')) return 'badge-primary';
        if (action.includes('CANCEL')) return 'badge-warning';
        if (action.includes('COMPLETE')) return 'badge-success';
        return 'badge-default';
    };

    if (loading) return <div className="loading">Loading...</div>;

    return (
        <div className="page">
            <div className="page-header">
                <h1>📊 Activity Log</h1>
                <div className="filter-bar">
                    <input
                        type="text"
                        placeholder="🔍 Filter log..."
                        value={filter}
                        onChange={(e) => setFilter(e.target.value)}
                        className="filter-input"
                    />
                </div>
            </div>

            <div className="table-container glass">
                <table>
                    <thead>
                        <tr>
                            <th>Waktu</th><th>User</th><th>Role</th><th>Aksi</th>
                            <th>Entity</th><th>ID</th><th>Detail</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredLogs.map(log => (
                            <tr key={log.id}>
                                <td><small>{new Date(log.timestamp).toLocaleString('id-ID')}</small></td>
                                <td><strong>{log.username}</strong></td>
                                <td><span className={`badge ${log.role === 'ADMIN' ? 'badge-info' : 'badge-default'}`}>{log.role}</span></td>
                                <td><span className={`badge ${actionBadge(log.action)}`}>{log.action}</span></td>
                                <td>{log.entityName}</td>
                                <td>{log.entityId || '-'}</td>
                                <td><small>{log.details}</small></td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {filteredLogs.length === 0 && <p className="empty-state">Tidak ada log ditemukan</p>}
            </div>
        </div>
    );
}
