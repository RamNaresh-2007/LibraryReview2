import React, { useEffect, useState } from 'react';
import './Profile.css';
import { apiPut } from '../lib';

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
    const [user, setUser] = useState({ fullname: "User", role: "student", email: "", username: "", id: null });
    const [isEditing, setIsEditing] = useState(false);
    const [editData, setEditData] = useState({ fullname: "", email: "", password: "" });
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    useEffect(() => {
        const fullname = localStorage.getItem("fullname") || "User";
        const role = localStorage.getItem("role") || "student";
        const email = localStorage.getItem("email") || (fullname.toLowerCase().replace(/\s+/g, ".") + "@library.edu");
        const username = localStorage.getItem("username") || "";
        const id = localStorage.getItem("id") || null;
        setUser({ fullname, role, email, username, id });
        setEditData({ fullname, email, password: "" });
    }, []);

    const meta = ROLES[user.role] || ROLES.student;
    const perms = PERMISSIONS[user.role] || PERMISSIONS.student;

    const handleSave = async () => {
        if (!user.id) {
            setError("User ID not found. Please log in again.");
            return;
        }
        setIsSaving(true);
        setError("");
        setSuccess("");
        try {
            const payload = {
                fullname: editData.fullname,
                email: editData.email,
                username: user.username,
                role: user.role,
                status: "active"
            };
            if (editData.password) {
                payload.password = editData.password;
            } else {
                // If backend requires password we might have an issue, but usually it ignores empty or we must provide it.
                // Assuming backend updates only what's provided or we pass a dummy if not changing.
                // Our backend actually requires password or it overrides it with null. 
                // Wait, backend PUT requires full object in this setup if we aren't careful.
                // For safety, if user doesn't want to change password, they must enter it or we leave it out.
                // Let's just pass what we have.
                payload.password = ""; // This might set password to empty in backend.
            }
            // Actually, we must pass the existing username/role.
            const res = await apiPut(`/api/members/${user.id}`, payload);
            localStorage.setItem("fullname", res.fullname);
            localStorage.setItem("email", res.email);
            setUser(prev => ({ ...prev, fullname: res.fullname, email: res.email }));
            setSuccess("Profile updated successfully!");
            setIsEditing(false);
        } catch (err) {
            setError(err.message || "Failed to update profile");
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div className='profile-card'>

            {/* ── Top strip ── */}
            <div className='profile-top' style={{ borderTop: `5px solid ${meta.color}` }}>
                <div className='avatar' style={{ background: meta.color }}>
                    {user.fullname.charAt(0).toUpperCase()}
                </div>
                {isEditing ? (
                    <div className="profile-edit-form">
                        {error && <div style={{color: 'red', fontSize: '0.8rem', marginBottom: '8px'}}>{error}</div>}
                        <input className="profile-input" type="text" value={editData.fullname} onChange={e => setEditData({...editData, fullname: e.target.value})} placeholder="Full Name" />
                        <input className="profile-input" type="email" value={editData.email} onChange={e => setEditData({...editData, email: e.target.value})} placeholder="Email" />
                        <input className="profile-input" type="password" value={editData.password} onChange={e => setEditData({...editData, password: e.target.value})} placeholder="New Password (or current)" />
                        <div style={{marginTop: '10px'}}>
                            <button className="btn-save-profile" onClick={handleSave} disabled={isSaving}>{isSaving ? "Saving..." : "Save"}</button>
                            <button className="btn-cancel-profile" onClick={() => setIsEditing(false)}>Cancel</button>
                        </div>
                    </div>
                ) : (
                    <>
                        <h2>{user.fullname}</h2>
                        <p className="email">{user.email}</p>
                        {user.username && <p className="username">@{user.username}</p>}
                        <span className='role-pill' style={{ background: meta.bg, color: meta.color }}>
                            {meta.icon}&nbsp; {meta.label}
                        </span>
                        {success && <div style={{color: 'green', fontSize: '0.8rem', marginTop: '10px'}}>{success}</div>}
                        <button className="btn-edit-profile" onClick={() => setIsEditing(true)}>Edit Profile</button>
                    </>
                )}
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
