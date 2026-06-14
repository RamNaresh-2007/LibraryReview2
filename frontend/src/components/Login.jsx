import React, { useState, useRef, useEffect } from 'react';
import { apiPost } from '../lib';
import ProgressBar from './ProgressBar';
import '../App.css'; // Shared layout styles

const ROLES = [
    { value: "student", label: "Student" },
    { value: "teacher", label: "Teacher" },
    { value: "librarian", label: "Librarian" },
    { value: "admin", label: "Admin" },
];

const Login = ({ onLoginSuccess }) => {
    const [isSignin, setIsSignIn] = useState(true);
    const [isProgress, setIsProgress] = useState(false);
    const [errorData, setErrorData] = useState({});
    const [apiError, setApiError] = useState("");
    const [successMsg, setSuccessMsg] = useState("");
    const finput = useRef();

    const [signupData, setSignupData] = useState({ fullname: "", email: "", username: "", password: "", role: "student" });
    const [signinData, setSigninData] = useState({ username: "", password: "" });

    useEffect(() => {
        setTimeout(() => finput.current?.focus(), 100);
    }, [isSignin]);

    const switchWindow = () => {
        setIsSignIn(p => !p);
        setErrorData({});
        setApiError("");
        setSuccessMsg("");
    };

    const handle = (setter, state) => (e) => setter({ ...state, [e.target.name]: e.target.value });

    const validate = (fields) => {
        const errors = {};
        fields.forEach(f => { if (!f.val) errors[f.key] = true; });
        setErrorData(errors);
        return Object.keys(errors).length > 0;
    };

    const handleAuthResponse = (data) => {
        setIsProgress(false);
        if (data.token) {
            localStorage.setItem("token", data.token);
            if (data.id) localStorage.setItem("id", data.id);
            localStorage.setItem("fullname", data.fullname || data.username);
            localStorage.setItem("role", data.role);
            localStorage.setItem("username", data.username);
            localStorage.setItem("email", data.email || "");
            onLoginSuccess();
        }
    };

    const handleAuthError = (err) => {
        setIsProgress(false);
        setApiError(err?.message || "Server connection lost. Check if Backend is running.");
    };

    const signin = async () => {
        if (validate([
            { key: 'username', val: signinData.username },
            { key: 'password', val: signinData.password }
        ])) return;

        setIsProgress(true);
        setApiError("");

        try {
            const data = await apiPost('/api/auth/login', signinData);
            handleAuthResponse(data);
        } catch (err) {
            handleAuthError(err);
        }
    };

    const signup = async () => {
        if (validate([
            { key: 'fullname', val: signupData.fullname },
            { key: 'username', val: signupData.username },
            { key: 'email', val: signupData.email },
            { key: 'password', val: signupData.password }
        ])) return;

        setIsProgress(true);
        setApiError("");

        try {
            await apiPost('/api/auth/register', signupData);
            setIsProgress(false);
            setSuccessMsg("Account created successfully! Please sign in.");
            setIsSignIn(true);
            setSigninData(prev => ({ ...prev, username: signupData.username }));
        } catch (err) {
            handleAuthError(err);
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') isSignin ? signin() : signup();
    };

    return (
        <div className='app-viewport animate-in'>
            <div className='auth-bg'></div>
            <div className='login-container'>
                <div className='login-header'>
                    <span className="logo-circ">📚</span>
                    <h1>Digital Library</h1>
                    <p>{isSignin ? "Sign in to your workspace" : "Join the digital network"}</p>
                </div>

                <div className='login-content'>
                    {apiError && <div className='api-error'>{apiError}</div>}
                    {successMsg && <div className='success-msg'>{successMsg}</div>}

                    {isSignin ? (
                        <>
                            <label>Username</label>
                            <input type='text' ref={finput} className={errorData.username ? 'error' : ''} placeholder='Enter username' autoComplete='off' name="username" value={signinData.username} onChange={handle(setSigninData, signinData)} onKeyDown={handleKeyDown} />

                            <label>Password</label>
                            <input type='password' className={errorData.password ? 'error' : ''} placeholder='Enter password' name='password' value={signinData.password} onChange={handle(setSigninData, signinData)} onKeyDown={handleKeyDown} />

                            <button className="btn-login" onClick={signin} disabled={isProgress}>
                                {isProgress ? "Validating…" : "Sign In"}
                            </button>
                            <p className="toggle-auth">New here? <span onClick={switchWindow}>Create account</span></p>
                        </>
                    ) : (
                        <>
                            <label>Full Name</label>
                            <input type='text' ref={finput} className={errorData.fullname ? 'error' : ''} placeholder='Enter your full name' name='fullname' value={signupData.fullname} onChange={handle(setSignupData, signupData)} onKeyDown={handleKeyDown} />

                            <label>Username</label>
                            <input type='text' className={errorData.username ? 'error' : ''} placeholder='Choose unique username' name='username' value={signupData.username} onChange={handle(setSignupData, signupData)} onKeyDown={handleKeyDown} />

                            <label>Email</label>
                            <input type='email' className={errorData.email ? 'error' : ''} placeholder='Enter email address' name='email' value={signupData.email} onChange={handle(setSignupData, signupData)} onKeyDown={handleKeyDown} />

                            <label>Role</label>
                            <select name='role' value={signupData.role} onChange={handle(setSignupData, signupData)} className='field-select'>
                                {ROLES.map(r => <option key={r.value} value={r.value}>{r.label}</option>)}
                            </select>

                            <label>Password</label>
                            <input type='password' className={errorData.password ? 'error' : ''} placeholder='Create strong password' name='password' value={signupData.password} onChange={handle(setSignupData, signupData)} onKeyDown={handleKeyDown} />

                            <button className="btn-login" onClick={signup} disabled={isProgress}>
                                {isProgress ? "Creating…" : "Register Now"}
                            </button>
                            <p className="toggle-auth">Already a member? <span onClick={switchWindow}>Sign In</span></p>
                        </>
                    )}
                </div>

                <div className='login-footer' style={{ marginTop: 32, fontSize: '0.8rem', color: '#64748b', textAlign: 'center', opacity: 0.6 }}>&copy; 2026 Library Management System</div>
            </div>
            <ProgressBar isProgress={isProgress} />
        </div>
    );
};

export default Login;
