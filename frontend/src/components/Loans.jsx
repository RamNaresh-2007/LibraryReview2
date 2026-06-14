import React, { useEffect, useState } from 'react';
import './Loans.css';
import { apiGet, apiPost, apiPatch, apiDelete } from '../lib';

const ROLE_COLORS = { student: "#2e7d32", teacher: "#1565c0", librarian: "#e65100", admin: "#6a1b9a" };

const Loans = ({ role }) => {
    const [loans, setLoans] = useState([]);
    const [filter, setFilter] = useState('all');
    const [search, setSearch] = useState('');
    const [showIssue, setShowIssue] = useState(false);
    const [newLoan, setNewLoan] = useState({ memberName: '', memberRole: 'student', bookTitle: '', isbn: '', dueDate: '' });
    const [isLoading, setIsLoading] = useState(false);
    const [apiError, setApiError] = useState('');

    const canManage = role === 'admin' || role === 'librarian';

    const today = new Date().toISOString().split('T')[0];

    const fetchLoans = async () => {
        setIsLoading(true);
        try {
            const data = await apiGet('/api/loans');
            setLoans(data || []);
        } catch (err) {
            setApiError('Load failure');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => { fetchLoans(); }, []);

    // ── derived (optimized) ────────────────────────────────────────
    const filtered = React.useMemo(() => {
        return loans.filter(l => {
            const matchFilter = filter === 'all' || (filter === 'overdue' ? l.dueDate < today : l.status === filter);
            const matchSearch = (l.memberName || '').toLowerCase().includes(search.toLowerCase()) ||
                (l.bookTitle || '').toLowerCase().includes(search.toLowerCase());
            return matchFilter && matchSearch;
        });
    }, [loans, filter, search, today]);

    const activeCount = React.useMemo(() => loans.filter(l => l.status !== 'returned').length, [loans]);
    const overdueCount = React.useMemo(() => loans.filter(l => l.dueDate < today && l.status !== 'returned').length, [loans, today]);

    // ── actions ────────────────────────────────────────────────────
    const returnBook = async (id) => {
        if (!canManage) return;
        if (!window.confirm('Mark this book as returned?')) return;
        try {
            const updated = await apiPatch(`/api/loans/${id}/return`);
            setLoans(prev => prev.map(l => l.id === updated.id ? updated : l));
        } catch (err) {
            setApiError('Return failed');
        }
    };

    const deleteLoan = async (id) => {
        if (!canManage) return;
        if (!window.confirm('Delete this loan record?')) return;
        try {
            await apiDelete(`/api/loans/${id}`);
            setLoans(prev => prev.filter(l => l.id !== id));
        } catch (err) {
            setApiError(err.message || 'Delete failed');
        }
    };

    const issueBook = async () => {
        if (!canManage) return;
        if (!newLoan.memberName || !newLoan.bookTitle || !newLoan.dueDate) {
            setApiError('Member name, book title, and due date are required');
            return;
        }
        setApiError('');
        setIsLoading(true);

        const payload = {
            memberName: newLoan.memberName,
            memberRole: newLoan.memberRole || 'student',
            bookTitle: newLoan.bookTitle,
            isbn: newLoan.isbn || '—',
            dueDate: newLoan.dueDate,
        };

        try {
            // CO5: Call the Saga Orchestrator instead of direct Spring Boot endpoint
            const saved = await apiPost('/api/saga/issue-book', payload);
            setLoans(prev => [...prev, saved]);
            setNewLoan({ memberName: '', memberRole: 'student', bookTitle: '', isbn: '', dueDate: '' });
            setShowIssue(false);
        } catch (err) {
            setApiError(err.message || 'Issue failed');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className='loans-page'>
            <div className='loans-header'>
                <div>
                    <h2>Loan Management</h2>
                    {overdueCount > 0 && (
                        <span className='overdue-alert'>⚠️ {overdueCount} overdue loan{overdueCount > 1 ? 's' : ''}</span>
                    )}
                </div>
                {canManage && <button className='btn-issue' onClick={() => setShowIssue(true)}>+ Issue Book</button>}
            </div>

            {apiError && <div className='api-error-banner'>{apiError}</div>}

            <div className='loans-toolbar'>
                <div className='filter-tabs'>
                    {['all', 'active', 'overdue'].map(f => (
                        <button key={f} className={`tab ${filter === f ? 'active' : ''}`} onClick={() => setFilter(f)}>
                            {f === 'all' ? `All (${activeCount})` : f.charAt(0).toUpperCase() + f.slice(1)}
                        </button>
                    ))}
                </div>
                <input className='search-box' placeholder='🔍 Search member or book…' value={search} onChange={e => setSearch(e.target.value)} />
            </div>

            <div className='loans-table-wrap'>
                <table className='loans-table'>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Member</th>
                            <th>Book</th>
                            <th>Issued</th>
                            <th>Due Date</th>
                            <th>Status</th>
                            {canManage && <th>Action</th>}
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading && loans.length === 0 && (
                            <tr><td colSpan={7} style={{ textAlign: 'center', padding: '30px', color: '#ccc' }}>Loading active loans…</td></tr>
                        )}
                        {!isLoading && filtered.length === 0 && (
                            <tr><td colSpan={7} style={{ textAlign: 'center', padding: '30px', color: '#ccc' }}>No loans found</td></tr>
                        )}
                        {filtered.map((l, i) => {
                            const isOverdue = l.dueDate < today && l.status !== 'returned';
                            return (
                                <tr key={l.id} className={isOverdue ? 'row-overdue' : ''}>
                                    <td>{i + 1}</td>
                                    <td>
                                        <div className='member-cell'>
                                            <div className='m-avatar' style={{ background: ROLE_COLORS[l.memberRole] || '#666' }}>
                                                {(l.memberName || 'U').charAt(0)}
                                            </div>
                                            <div>
                                                <div className='m-name'>{l.memberName}</div>
                                                <div className='m-role'>{l.memberRole}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <div className='book-title'>{l.bookTitle}</div>
                                        <div className='book-isbn'>{l.isbn}</div>
                                    </td>
                                    <td>{l.issuedDate}</td>
                                    <td className={isOverdue ? 'due-overdue' : ''}>{l.dueDate}</td>
                                    <td>
                                        <span className={`status-badge ${l.status === 'returned' ? 'returned' : isOverdue ? 'overdue' : 'active'}`}>
                                            {l.status === 'returned' ? 'Returned' : isOverdue ? 'Overdue' : 'Active'}
                                        </span>
                                    </td>
                                    {canManage && (
                                        <td>
                                            <div style={{ display: 'flex', gap: 6 }}>
                                                <button className='btn-return' onClick={() => returnBook(l.id)}>Return</button>
                                                <button className='btn-trash' onClick={() => deleteLoan(l.id)}>🗑</button>
                                            </div>
                                        </td>
                                    )}
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>

            {showIssue && (
                <div className='overlay'>
                    <div className='issue-popup'>
                        <span className='close-btn' onClick={() => setShowIssue(false)}>&times;</span>
                        <h3>📤 Issue New Book</h3>

                        {apiError && <div style={{ color: '#e53e3e', marginBottom: 10, fontSize: 13 }}>{apiError}</div>}

                        <label>Member Name *</label>
                        <input placeholder='Enter member name' value={newLoan.memberName} onChange={e => setNewLoan(p => ({ ...p, memberName: e.target.value }))} />

                        <label>Member Role</label>
                        <select value={newLoan.memberRole} onChange={e => setNewLoan(p => ({ ...p, memberRole: e.target.value }))}>
                            {['student', 'teacher', 'librarian', 'admin'].map(r => (
                                <option key={r} value={r}>{r.charAt(0).toUpperCase() + r.slice(1)}</option>
                            ))}
                        </select>

                        <label>Book Title *</label>
                        <input placeholder='Enter book title' value={newLoan.bookTitle} onChange={e => setNewLoan(p => ({ ...p, bookTitle: e.target.value }))} />

                        <label>ISBN (optional)</label>
                        <input placeholder='Enter ISBN' value={newLoan.isbn} onChange={e => setNewLoan(p => ({ ...p, isbn: e.target.value }))} />

                        <label>Due Date *</label>
                        <input type='date' min={today} value={newLoan.dueDate} onChange={e => setNewLoan(p => ({ ...p, dueDate: e.target.value }))} />

                        <button className='btn-confirm' onClick={issueBook} disabled={isLoading}>
                            {isLoading ? 'Issuing…' : 'Confirm Issue'}
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Loans;
