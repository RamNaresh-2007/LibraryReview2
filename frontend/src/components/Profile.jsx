import React, { useEffect, useState } from 'react';
import './Profile.css';

const ROLES = {
    student: { label: "Student", icon: "🎓", color: "#2e7d32", bg: "#e8f5e9", borrowLimit: 5, renewals: 2 },
    teacher: { label: "Teacher", icon: "📝", color: "#1565c0", bg: "#e3f2fd", borrowLimit: 15, renewals: "∞" },
    librarian: { label: "Librarian", icon: "🔖", color: "#e65100", bg: "#fff3e0", borrowLimit: 20, renewals: "∞" },
    admin: { label: "Admin", icon: "⚙️", color: "#6a1b9a", bg: "#f3e5f5", borrowLimit: "∞", renewals: "∞" },
};

/* Role-specific key capabilities (max 4 shown) */
const PERMISSIONS = {
    student: ["Browse & borrow books", "Renew books up to 2 times", "Reserve study rooms (2 hrs max)", "Access digital e-books"],
    teacher: ["Borrow up to 15 books", "Unlimited renewals", "Priority holds on new arrivals", "Access research journal database"],
    librarian: ["Manage book catalog", "Process loans & returns", "View overdue & inventory reports", "Register new acquisitions"],
    admin: ["Full system access", "Manage all users & roles", "View audit logs & activity", "Configure library policies"],
};

const Profile = () => {
    const [user, setUser] = useState({ fullname: "User", role: "student", email: "" });

    useEffect(() => {
        const fullname = localStorage.getItem("fullname") || "User";
        const role = localStorage.getItem("role") || "student";
        const email = localStorage.getItem("email") || (fullname.toLowerCase().replace(/\s+/g, ".") + "@library.edu");
        const username = localStorage.getItem("username") || "";
        setUser({ fullname, role, email, username });
    }, []);

    const meta = ROLES[user.role] || ROLES.student;
    const perms = PERMISSIONS[user.role] || PERMISSIONS.student;

    return (
        <div className='profile-card'>

            {/* ── Top strip ── */}
            <div className='profile-top' style={{ borderTop: `5px solid ${meta.color}` }}>
                <div className='avatar' style={{ background: meta.color }}>
                    {user.fullname.charAt(0).toUpperCase()}
                </div>
                <h2>{user.fullname}</h2>
                <p className="email">{user.email}</p>
                {user.username && <p className="username">@{user.username}</p>}
                <span className='role-pill' style={{ background: meta.bg, color: meta.color }}>
                    {meta.icon}&nbsp; {meta.label}
                </span>
            </div>

            {/* ── Stats row ── */}
            <div className='stats-row'>
                <div className='stat'>
                    <span>{meta.borrowLimit}</span>
                    <label>Borrow Limit</label>
                </div>
                <div className='stat'>
                    <span>{meta.renewals}</span>
                    <label>Max Renewals</label>
                </div>
                <div className='stat'>
                    <span>{user.role === 'admin' || user.role === 'librarian' ? "Full" : "Standard"}</span>
                    <label>Access Level</label>
                </div>
            </div>

            {/* ── Permissions ── */}
            <div className='perms-section'>
                <h4>Key Permissions</h4>
                <ul>
                    {perms.map((p, i) => (
                        <li key={i}>
                            <span className='check' style={{ color: meta.color }}>✓</span> {p}
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
};

export default Profile;
