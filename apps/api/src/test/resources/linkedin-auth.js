// linkedin-auth.js
// JS logic for LinkedIn authentication demo

const API_BASE = 'http://localhost:8080';
const apiKey = '7e2b4c8f1a3d4e5f9b6c2a1e8d7f0b3';

export async function loginWithLinkedIn() {
    // Use POST and send apiKey in the body to avoid CORS issues with custom headers
    const url = `${API_BASE}/auth/linkedin/loginUrl`;
    try {
        const res = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-API-KEY': apiKey
            },
        });
        if (!res.ok) throw new Error('Failed to get LinkedIn login URL');
        window.location.href = await res.text();
    } catch (err) {
        alert('Error: ' + err.message);
    }
}


function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}


export async function checkAuth() {
    try {
        const headers = {'X-API-KEY': apiKey, 'Content-Type': 'application/json'};
        const res = await fetch(`${API_BASE}/auth/isAuthenticated`, {
            headers,
            credentials: 'include' // This will send cookies (including refresh token) automatically
        });
        if (!res.ok) throw new Error('Not authenticated');
        const data = await res.json();
        document.getElementById('auth-status').textContent = data === true ? 'Authenticated' : 'Not Authenticated';
    } catch (err) {
        document.getElementById('auth-status').textContent = 'Not Authenticated';
    }
}

export async function logout() {
    try {
        const headers = {'X-API-KEY': apiKey, 'Content-Type': 'application/json'};
        const res = await fetch(`${API_BASE}/auth/logout`, {
            method: 'delete',
            headers,
            credentials: 'include'
        });
        if (!res.ok) throw new Error('Logout failed');
        document.getElementById('auth-status').textContent = 'Not Authenticated';
        alert('Logged out!');
    } catch (err) {
        alert('Logout error: ' + err.message);
    }
}

export function setupLinkedInAuthDemo() {
    document.getElementById('login-btn').onclick = loginWithLinkedIn;
    document.getElementById('logout-btn').onclick = logout;
    checkAuth();
}
