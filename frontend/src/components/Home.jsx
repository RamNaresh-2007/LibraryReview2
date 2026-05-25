import React, { useEffect, useState } from 'react';
import './Home.css';
import ProgressBar from './ProgressBar';
import Dashboard from './Dashboard';
import BookManager from './BookManager';
import Loans from './Loans';
import Members from './Members';
import Profile from './Profile';

const NAV = [
    { key: 'dashboard', icon: '📊', label: 'Dashboard' },
    { key: 'books', icon: '📖', label: 'Books' },
    { key: 'loans', icon: '🔄', label: 'Loans' },
    { key: 'members', icon: '👥', label: 'Members' },
    { key: 'profile', icon: '👤', label: 'My Profile' },
];

const Home = ({ onLogout }) => {
    const [fullname, setFullname] = useState("Admin");
    const [role, setRole] = useState("admin");
    const [activeMenu, setActiveMenu] = useState('dashboard');
    const [notifOpen, setNotifOpen] = useState(false);

    const NOTIFS = [
        { text: '"Atomic Habits" is overdue — Priya Singh', type: 'alert', time: '2 hr ago' },
        { text: '"DBMS" overdue by Amit Yadav', type: 'alert', time: '3 hr ago' },
        { text: 'New member: Anjali Mehta (Student)', type: 'info', time: '1 hr ago' },
        { text: '"Clean Code" issued to Rahul Sharma', type: 'info', time: '2 min ago' },
    ];

    useEffect(() => {
        const n = localStorage.getItem("fullname");
        const r = localStorage.getItem("role");
        if (n) setFullname(n);
        if (r) setRole(r);
    }, []);

    const logout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("fullname");
        localStorage.removeItem("role");
        localStorage.removeItem("username");
        if (onLogout) onLogout();
        else window.location.replace("/");
    };

    const renderContent = () => {
        switch (activeMenu) {
            case 'dashboard': return <Dashboard role={role} />;
            case 'books': return <BookManager role={role} />;
            case 'loans': return <Loans role={role} />;
            case 'members': return <Members role={role} />;
            case 'profile': return <Profile role={role} />;
            default: return <Dashboard role={role} />;
        }
    };

    const ROLE_COLORS = { admin: "#6a1b9a", librarian: "#e65100", teacher: "#1565c0", student: "#2e7d32" };

    // ── Filter Nav based on role ──────────────────────────────────
    const filteredNav = NAV.filter(item => {
        if (role === 'admin') return true;
        if (role === 'librarian') return item.key !== 'members';
        // Teachers and Students only see Dashboard, Books, and Profile
        return ['dashboard', 'books', 'profile'].includes(item.key);
    });

    return (
        <div className='home'>
            {/* ── top header ── */}
            <div className='home-header'>
                <div className="logo-section">
                    <span className="logo-book">📚</span>
                    <div>
                        <span className="logo-name">Digital Library</span>
                        <span className="logo-sub">Management System</span>
                    </div>
                </div>
                <div className='header-right'>
                    {/* notification bell */}
                    <div className='notif-wrap'>
                        <button className='notif-btn' onClick={() => setNotifOpen(o => !o)}>
                            🔔
                            <span className='notif-count'>2</span>
                        </button>
                        {notifOpen && (
                            <div className='notif-dropdown'>
                                <div className='notif-header'>Notifications</div>
                                {NOTIFS.map((n, i) => (
                                    <div key={i} className={`notif-item ${n.type}`}>
                                        <span>{n.type === 'alert' ? '⚠️' : 'ℹ️'}</span>
                                        <div>
                                            <p>{n.text}</p>
                                            <small>{n.time}</small>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                    {/* user pill */}
                    <div className='user-pill'>
                        <div className='user-ava' style={{ background: ROLE_COLORS[role] || '#01559C' }}>
                            {fullname.charAt(0).toUpperCase()}
                        </div>
                        <div>
                            <div className='user-name'>{fullname}</div>
                            <div className='user-role' style={{ color: ROLE_COLORS[role] }}>{role}</div>
                        </div>
                    </div>
                    <button className="logout-btn" onClick={logout}>Logout</button>
                </div>
            </div>

            <div className='home-workspace'>
                {/* ── sidebar ── */}
                <div className='home-menus'>
                    {filteredNav.map(n => (
                        <div
                            key={n.key}
                            className={`menu-item ${activeMenu === n.key ? 'active' : ''}`}
                            onClick={() => setActiveMenu(n.key)}
                        >
                            <span className='menu-icon'>{n.icon}</span>
                            <span>{n.label}</span>
                        </div>
                    ))}
                </div>

                {/* ── main ── */}
                <div className='home-content'>
                    {renderContent()}
                </div>
            </div>

            <div className='home-footer'>
                &copy; 2026 Digital Library Management System &nbsp;·&nbsp; All rights reserved
            </div>
        </div>
    );
};

export default Home;
