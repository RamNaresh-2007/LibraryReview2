import React, { useEffect, useState } from 'react';
import './Members.css';
import { apiGet, apiPost, apiPut, apiPatch, apiDelete } from '../lib';

const ROLE_COLORS = { student: "#2e7d32", teacher: "#1565c0", librarian: "#e65100", admin: "#6a1b9a" };
const ROLE_BG = { student: "#e8f5e9", teacher: "#e3f2fd", librarian: "#fff3e0", admin: "#f3e5f5" };

const EMPTY_MEMBER = { fullname: '', username: '', email: '', role: 'student', password: '' };

const Members = () => {
    const [members, setMembers] = useState([]);
    const [search, setSearch] = useState('');
    const [roleFilter, setRoleFilter] = useState('all');
    const [isLoading, setIsLoading] = useState(false);
    const [apiError, setApiError] = useState('');
    const [showPopup, setShowPopup] = useState(false);
    const [memberData, setMemberData] = useState(EMPTY_MEMBER);
    const [isEditing, setIsEditing] = useState(false);

    const fetchMembers = async () => {
        setIsLoading(true);
        try {
            const data = await apiGet('/api/members');
            setMembers(data || []);
        } catch (err) {
            setApiError('Load failure');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => { fetchMembers(); }, []);

    // ── derived (optimized) ────────────────────────────────────────
    const filtered = React.useMemo(() => {
        return members.filter(m => {
            const matchRole = roleFilter === 'all' || m.role === roleFilter;
            const matchSearch = (m.fullname || '').toLowerCase().includes(search.toLowerCase()) ||
                (m.email || '').toLowerCase().includes(search.toLowerCase());
            return matchRole && matchSearch;
        });
    }, [members, roleFilter, search]);

    const roleCounts = React.useMemo(() => {
        const counts = { all: members.length };
        members.forEach(m => { counts[m.role] = (counts[m.role] || 0) + 1; });
        return counts;
    }, [members]);

    // ── actions ────────────────────────────────────────────────────
    const toggleStatus = async (id) => {
        try {
            const updated = await apiPatch(`/api/members/${id}/toggle-status`);
            setMembers(prev => prev.map(m => m.id === updated.id ? updated : m));
        } catch (err) {
            setApiError('Toggle failed');
        }
    };

    const deleteMember = async (id) => {
        if (!window.confirm('Remove this member?')) return;
        try {
            await apiDelete(`/api/members/${id}`);
            setMembers(prev => prev.filter(m => m.id !== id));
        } catch (err) {
            setApiError(err.message || 'Delete failed');
        }
    };

    const openAdd = () => {
        setMemberData(EMPTY_MEMBER);
        setIsEditing(false);
        setShowPopup(true);
    };

    const openEdit = (m) => {
        setMemberData({ ...m, password: '' });
        setIsEditing(true);
        setShowPopup(true);
    };

    const saveMember = async () => {
        if (!memberData.fullname || !memberData.email || !memberData.username) {
            setApiError('Full name, username, and email are required');
            return;
        }
        setApiError('');
        setIsLoading(true);

        const isEdit = !!memberData.id;
        const url = isEdit ? `/api/members/${memberData.id}` : `/api/members`;

        try {
            const saved = isEdit ? await apiPut(url, memberData) : await apiPost(url, memberData);
            setMembers(prev => isEdit
                ? prev.map(m => m.id === saved.id ? saved : m)
                : [...prev, saved]
            );
            setShowPopup(false);
        } catch (err) {
            setApiError(err.message || 'Save failed');
        } finally {
            setIsLoading(false);
        }
    };

    const handleInput = (e) => setMemberData(prev => ({ ...prev, [e.target.name]: e.target.value }));

    return (
        <div className='members-page'>
            <div className='members-header'>
                <h2>Members</h2>
                <div className='mh-right'>
                    <span className='total-badge'>{members.length} total</span>
                    <button className='btn-add-member' onClick={openAdd}>+ Add Member</button>
                </div>
            </div>

            {apiError && <div className='api-error-banner'>{apiError}</div>}

            <div className='role-pills'>
                {['all', 'student', 'teacher', 'librarian', 'admin'].map(r => (
                    <button
                        key={r}
                        className={`role-pill-btn ${roleFilter === r ? 'active' : ''}`}
                        style={roleFilter === r && r !== 'all' ? { background: ROLE_COLORS[r], borderColor: ROLE_COLORS[r], color: 'white' } : {}}
                        onClick={() => setRoleFilter(r)}
                    >
                        {r.charAt(0).toUpperCase() + r.slice(1)} ({roleCounts[r] || 0})
                    </button>
                ))}
                <input className='members-search' placeholder='🔍 Search name or email…' value={search} onChange={e => setSearch(e.target.value)} />
            </div>

            <div className='members-grid'>
                {isLoading && members.length === 0 && <div className='no-members'>Loading directory…</div>}
                {filtered.map(m => (
                    <div className='member-card' key={m.id}>
                        <div className='mc-top'>
                            <div className='mc-avatar' style={{ background: ROLE_COLORS[m.role] || '#666' }}>
                                {(m.fullname || 'U').charAt(0)}
                            </div>
                            <div>
                                <div className='mc-name'>{m.fullname}</div>
                                <div className='mc-email'>{m.email}</div>
                            </div>
                        </div>
                        <div className='mc-tags'>
                            <span className='mc-role' style={{ background: ROLE_BG[m.role], color: ROLE_COLORS[m.role] }}>{m.role}</span>
                            <span className={`mc-status ${m.status}`}>{m.status}</span>
                        </div>
                        <div className='mc-info'>
                            <div><label>Joined</label><span>{m.joinedDate || '—'}</span></div>
                            <div><label>Active Loans</label><span>{m.loans}</span></div>
                        </div>
                        <div className='mc-actions'>
                            <button className={`btn-status ${m.status}`} onClick={() => toggleStatus(m.id)}>
                                {m.status === 'active' ? 'Suspend' : 'Activate'}
                            </button>
                            <button className='btn-edit-mem' onClick={() => openEdit(m)}>✏️</button>
                            <button className='btn-del-mem' onClick={() => deleteMember(m.id)}>🗑</button>
                        </div>
                    </div>
                ))}
                {!isLoading && filtered.length === 0 && <div className='no-members'>No results found</div>}
            </div>

            {showPopup && (
                <div className='overlay'>
                    <div className='member-popup'>
                        <span className='close-x' onClick={() => setShowPopup(false)}>&times;</span>
                        <h3>{isEditing ? '✏️ Edit Member' : '👤 Add New Member'}</h3>

                        {apiError && <div style={{ color: '#e53e3e', marginBottom: 10, fontSize: 13 }}>{apiError}</div>}

                        <label>Full Name *</label>
                        <input name='fullname' placeholder='Enter full name' value={memberData.fullname} onChange={handleInput} />

                        <label>Username *</label>
                        <input name='username' placeholder='Enter username' value={memberData.username} onChange={handleInput} />

                        <label>Email *</label>
                        <input name='email' placeholder='Enter email' value={memberData.email} onChange={handleInput} />

                        <label>Role</label>
                        <select name='role' value={memberData.role} onChange={handleInput}>
                            {['student', 'teacher', 'librarian', 'admin'].map(r => (
                                <option key={r} value={r}>{r.charAt(0).toUpperCase() + r.slice(1)}</option>
                            ))}
                        </select>

                        <label>{isEditing ? 'New Password' : 'Password *'}</label>
                        <input type='password' name='password' placeholder='Enter password' value={memberData.password} onChange={handleInput} />

                        <button className='btn-save' onClick={saveMember} disabled={isLoading}>
                            {isLoading ? 'Saving…' : isEditing ? 'Save Changes' : 'Add Member'}
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Members;
