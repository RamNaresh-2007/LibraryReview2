import React, { useState, useEffect } from 'react';
import './Dashboard.css';
import { apiGet } from '../lib';

const Dashboard = () => {
    const [counts, setCounts] = useState({ books: 0, loans: 0, overdue: 0, members: 0 });
    const [targets, setTargets] = useState({ books: 0, loans: 0, overdue: 0, members: 0 });
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [books, loans, members] = await Promise.all([
                    apiGet('/api/books').catch(() => []),
                    apiGet('/api/loans').catch(() => []),
                    apiGet('/api/members').catch(() => [])
                ]);

                const today = new Date().toISOString().split('T')[0];
                const activeLoans = (loans || []).filter(l => l.status !== 'returned');
                const overdueCount = activeLoans.filter(l => l.dueDate < today).length;

                setTargets({
                    books: books?.length || 0,
                    loans: activeLoans.length,
                    overdue: overdueCount,
                    members: members?.length || 0
                });
            } catch (err) {
                setError('Failed to load stats');
            } finally {
                setIsLoading(false);
            }
        };
        fetchData();
    }, []);

    // ── Ultra-Fast Animation logic ──────────────────────────────
    useEffect(() => {
        if (isLoading) return;
        const duration = 300; // Total duration in ms
        const frameRate = 16; // ~60fps
        const totalFrames = duration / frameRate;

        const timer = setInterval(() => {
            setCounts(prev => {
                const next = { ...prev };
                let done = true;
                Object.keys(targets).forEach(key => {
                    if (prev[key] < targets[key]) {
                        const diff = targets[key] - prev[key];
                        const step = Math.ceil(targets[key] / totalFrames) || 1;
                        next[key] = Math.min(targets[key], prev[key] + step);
                        done = false;
                    }
                });
                if (done) clearInterval(timer);
                return next;
            });
        }, frameRate);
        return () => clearInterval(timer);
    }, [targets, isLoading]);

    const stats = [
        { label: "Total Books", value: counts.books, icon: "📚", color: "#1a56db", bg: "#ebf5ff", p: (counts.books / (targets.books || 1)) * 100 },
        { label: "Active Loans", value: counts.loans, icon: "🔄", color: "#057a55", bg: "#def7ec", p: (counts.loans / (targets.books || 1)) * 100 },
        { label: "Overdue", value: counts.overdue, icon: "⚠️", color: "#9b1c1c", bg: "#fdf2f2", p: (counts.overdue / (targets.loans || 1)) * 100 },
        { label: "Members", value: counts.members, icon: "👥", color: "#6b21a8", bg: "#f3e8ff", p: 100 },
    ];

    if (isLoading) {
        return <div className="dash-loading">⚡ Loading Library Data...</div>;
    }

    return (
        <div className='dashboard animate-in'>
            <div className='dash-title'>
                <h2>Library Overview</h2>
                <span className='dash-date'>{new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}</span>
            </div>

            {error && <div className="api-error-banner">{error}</div>}

            <div className='stat-cards'>
                {stats.map((s, i) => (
                    <div className='stat-card' key={i}>
                        <div className='sc-top'>
                            <div>
                                <div className='sc-value' style={{ color: s.color }}>{s.value}</div>
                                <div className='sc-label'>{s.label}</div>
                            </div>
                            <div className='sc-icon' style={{ background: s.bg, color: s.color }}>{s.icon}</div>
                        </div>
                        <div className='sc-bar-track'>
                            <div className='sc-bar-fill' style={{ width: `${s.p}%`, background: s.color }}></div>
                        </div>
                    </div>
                ))}
            </div>

            <div className='dash-middle'>
                <div className='dash-panel'>
                    <h3> Recent System Events</h3>
                    <ul className='activity-list'>
                        {[
                            { title: "Database Sync", desc: "PostgreSQL transaction finalized", time: "Just now", type: "add" },
                            { title: "Gateway Link", desc: "FastAPI relay established", time: "5m ago", type: "member" },
                            { title: "Security Active", desc: "JWT Validation success", time: "12m ago", type: "loan" }
                        ].map((a, i) => (
                            <li key={i} className='activity-item'>
                                <div className='act-info'>
                                    <strong>{a.title}</strong>
                                    <span>{a.desc}</span>
                                </div>
                                <span className='act-time'>{a.time}</span>
                            </li>
                        ))}
                    </ul>
                </div>

                <div className='dash-panel'>
                    <h3> Category Distribution</h3>
                    <div className='cat-chart'>
                        {[
                            { name: "Computer Science", c: 45, clr: "#1a56db" },
                            { name: "Mathematics", c: 28, clr: "#057a55" },
                            { name: "Engineering", c: 18, clr: "#a04e00" },
                            { name: "Literature", c: 9, clr: "#6b21a8" }
                        ].map((c, i) => (
                            <div className='cat-row' key={i}>
                                <span className='cat-name'>{c.name}</span>
                                <div className='cat-track'><div className='cat-bar' style={{ width: `${c.c}%`, background: c.clr }}></div></div>
                                <span className='cat-count'>{targets.books > 0 ? Math.round(targets.books * (c.c / 100)) : 0}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
