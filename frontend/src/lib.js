export const apibaseurl = import.meta.env.VITE_API_BASE_URL || "http://127.0.0.1:8000";

// ── JWT helpers ────────────────────────────────────────────────────
export const getToken = () => localStorage.getItem("token") || "";
export const getRole = () => localStorage.getItem("role") || "";
export const getFullname = () => localStorage.getItem("fullname") || "";
export const isLoggedIn = () => !!localStorage.getItem("token");

export const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("fullname");
    localStorage.removeItem("role");
    localStorage.removeItem("username");
    localStorage.removeItem("email");
    window.location.replace("/"); // Forces redirect on logout
};

// ── Promise-based API Wrapper ─────────────────────────────────────
export async function callApi(method, path, body = null) {
    const token = getToken();
    const headers = { "Content-Type": "application/json" };
    if (token) headers["Authorization"] = "Bearer " + token;

    const opts = { method, headers };
    if (body) opts.body = JSON.stringify(body);

    try {
        const res = await fetch(apibaseurl + path, opts);

        // Handle 401 Unauthorized globally
        if (res.status === 401) {
            const isLoginPage = window.location.pathname.includes('/login') || !isLoggedIn();
            if (!isLoginPage) {
                console.warn("Unauthorized! Clearing session...");
                logout();
                throw new Error("Your session has expired. Please log in again.");
            }
            // For login page, let the component handle the error
        }

        if (res.status === 204) return {};

        const contentType = res.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            const data = await res.json();
            if (!res.ok) throw data;
            return data;
        }

        if (!res.ok) throw { message: `Internal server error (${res.status})` };
        return {};
    } catch (err) {
        console.error("API Fetch Failure:", err);
        throw err;
    }
}

export const apiGet = (path) => callApi("GET", path);
export const apiPost = (path, body) => callApi("POST", path, body);
export const apiPut = (path, body) => callApi("PUT", path, body);
export const apiPatch = (path, body = null) => callApi("PATCH", path, body);
export const apiDelete = (path) => callApi("DELETE", path);
