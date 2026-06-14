import React, { useState, useEffect } from 'react';
import './Dashboard.css';
import { apiGet } from '../lib';

const Dashboard = () => {
    const [stats, setStats] = useState(null);
    const [topBooks, setTopBooks] = useState([]);
    const [trend, setTrend] = useState([]);
    const [activity, setActivity] = useState([]);
    const [myLoans, setMyLoans] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    
    const role = localStorage.getItem('role') || 'student';
    const username = localStorage.getItem('username') || '';
    const isStaff = role === 'admin' || role === 'librarian';

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                if (isStaff) {
                    // Global analytics for staff
                    const [dashStats, topB, loanTrend, logs] = await Promise.all([
                        apiGet('/api/analytics/dashboard'),
                        apiGet('/api/analytics/top-books'),
                        apiGet('/api/analytics/loan-trend'),
                        apiGet('/api/activity-log')
                    ]);

                    setStats(dashStats);
                    setTopBooks(Array.isArray(topB) ? topB : []);
                    setTrend(Array.isArray(loanTrend) ? loanTrend : []);
                    setActivity(Array.isArray(logs) ? logs : []);
                } else {
                    // Personal analytics for student/teacher
                    const loansData = await apiGet('/api/loans');
                    const personalLoans = loansData.filter(l => l.memberName === username);
                    setMyLoans(personalLoans);
                }
            } catch (err) {
                console.error("Dashboard Fetch Error:", err);
                setError('Failed to load dashboard data');
            } finally {
                setIsLoading(false);
            }
        };
        fetchDashboardData();
    }, [isStaff, username]);

    if (isLoading) return <div className="dash-loading">⚡ Loading Dashboard...</div>;

    const today = new Date().toISOString().split('T')[0];

    if (!isStaff) {
        // Personalized Dashboard for Students and Teachers
        const activeLoans = myLoans.filter(l => l.status === 'active');
        const overdueLoans = activeLoans.filter(l => l.dueDate < today);
        const returnedLoans = myLoans.filter(l => l.status === 'returned');

        return (
            <div className='dashboard animate-in'>
                <div className='dash-header'>
                    <div className='dash-title'>
                        <h2>My Dashboard</h2>
                        <p>Welcome back, {username}!</p>
                    </div>
                </div>

                {error && <div className="api-error-banner">{error}</div>}

                <div className='stat-cards'>
                    <div className='stat-card'>
                        <div className='sc-top'>
                            <div>
                                <div className='sc-value' style={{ color: "#1a56db" }}>{activeLoans.length}</div>
                                <div className='sc-label'>Active Loans</div>
                            </div>
                            <div className='sc-icon' style={{ background: "#ebf5ff", color: "#1a56db" }}>📚</div>
                        </div>
                    </div>
                    <div className='stat-card'>
                        <div className='sc-top'>
                            <div>
                                <div className='sc-value' style={{ color: "#9b1c1c" }}>{overdueLoans.length}</div>
                                <div className='sc-label'>Overdue Books</div>
                            </div>
                            <div className='sc-icon' style={{ background: "#fdf2f2", color: "#9b1c1c" }}>⚠️</div>
                        </div>
                    </div>
                    <div className='stat-card'>
                        <div className='sc-top'>
                            <div>
                                <div className='sc-value' style={{ color: "#057a55" }}>{returnedLoans.length}</div>
                                <div className='sc-label'>Books Returned</div>
                            </div>
                            <div className='sc-icon' style={{ background: "#def7ec", color: "#057a55" }}>✅</div>
                        </div>
                    </div>
                </div>

                <div className='dash-grid'>
                    <div className='dash-panel full-width'>
                        <h3>My Current Loans</h3>
                        {activeLoans.length === 0 ? (
                            <p style={{color: '#6b7280', fontSize: '0.9rem', padding: '16px 0'}}>You have no active loans at the moment.</p>
                        ) : (
                            <div className='activity-table-wrapper'>
                                <table className='activity-table'>
                                    <thead>
                                        <tr>
                                            <th>Book Title</th>
                                            <th>Issued Date</th>
                                            <th>Due Date</th>
                                            <th>Status</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {activeLoans.map((loan, i) => {
                                            const isOverdue = loan.dueDate < today;
                                            return (
                                                <tr key={i}>
                                                    <td><strong>{loan.bookTitle}</strong></td>
                                                    <td>{loan.issuedDate}</td>
                                                    <td style={isOverdue ? {color: '#dc2626', fontWeight: 600} : {}}>{loan.dueDate}</td>
                                                    <td>
                                                        <span style={{
                                                            background: isOverdue ? '#fee2e2' : '#dcfce7',
                                                            color: isOverdue ? '#dc2626' : '#166534',
                                                            padding: '4px 8px', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 600
                                                        }}>
                                                            {isOverdue ? 'OVERDUE' : 'ACTIVE'}
                                                        </span>
                                                    </td>
                                                </tr>
                                            );
                                        })}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        );
    }

    // Global Dashboard for Admins and Librarians
    const kpiCards = [
        { label: "Total Catalog", value: stats?.totalBooks || 0, icon: "📚", color: "#1a56db", bg: "#ebf5ff" },
        { label: "Active Loans", value: stats?.activeLoans || 0, icon: "🔄", color: "#057a55", bg: "#def7ec" },
        { label: "Overdue", value: stats?.overdueLoans || 0, icon: "⚠️", color: "#9b1c1c", bg: "#fdf2f2" },
        { label: "Members", value: stats?.totalMembers || 0, icon: "👥", color: "#6b21a8", bg: "#f3e8ff" },
    ];

    return (
        <div className='dashboard animate-in'>
            <div className='dash-header'>
                <div className='dash-title'>
                    <h2>Intelligence Dashboard</h2>
                </div>
            </div>

            {error && <div className="api-error-banner">{error}</div>}

            <div className='stat-cards'>
                {kpiCards.map((s, i) => (
                    <div className='stat-card' key={i}>
                        <div className='sc-top'>
                            <div>
                                <div className='sc-value' style={{ color: s.color }}>{s.value}</div>
                                <div className='sc-label'>{s.label}</div>
                            </div>
                            <div className='sc-icon' style={{ background: s.bg, color: s.color }}>{s.icon}</div>
                        </div>
                    </div>
                ))}
            </div>

            <div className='dash-grid'>
                {/* CO1: Window Functions (RANK) Visualization */}
                <div className='dash-panel'>
                    <h3>🔥 Most Popular Books (Window Rank)</h3>
                    <div className='popularity-list'>
                        {topBooks.map((b, i) => (
                            <div className='pop-item' key={i}>
                                <div className='pop-rank'>#{b.popularityRank}</div>
                                <div className='pop-info'>
                                    <strong>{b.title}</strong>
                                    <span>{b.author}</span>
                                </div>
                                <div className='pop-count'>{b.loanCount} loans</div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* CO1: Window Functions (LAG) Trend Visualization */}
                <div className='dash-panel'>
                    <h3>📈 Issuance Trend (MoM)</h3>
                    <div className='trend-list'>
                        {(() => {
                            const maxLoans = Math.max(...trend.map(t => t.loansIssued), 1);
                            return trend.slice(-4).map((t, i) => (
                                <div className='trend-item' key={i}>
                                    <span className='trend-month'>{t.month}</span>
                                    <div className='trend-bar-bg'>
                                        <div className='trend-bar-fill' style={{ width: `${(t.loansIssued / maxLoans) * 100}%` }}></div>
                                    </div>
                                    <span className='trend-count'>{t.loansIssued}</span>
                                    <span className={`trend-delta ${t.delta >= 0 ? 'pos' : 'neg'}`}>
                                        {t.delta > 0 ? `+${t.delta}` : t.delta}
                                    </span>
                                </div>
                            ));
                        })()}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
