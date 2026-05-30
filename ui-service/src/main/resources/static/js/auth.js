const API_BASE = 'http://localhost:8080';
const TOKEN_KEY = 'event_token';
const USER_KEY = 'event_user';

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function getUser() {
    const user = localStorage.getItem(USER_KEY);
    return user ? JSON.parse(user) : null;
}

function setAuthData(token, user) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
}

function clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
}

function getAuthHeaders() {
    const token = getToken();
    const headers = { 'Content-Type': 'application/json' };
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    return headers;
}

async function apiCall(url, options = {}) {
    const config = {
        ...options,
        headers: {
            ...getAuthHeaders(),
            ...options.headers,
        },
    };

    if (config.body && typeof config.body === 'object') {
        config.body = JSON.stringify(config.body);
    }

    const response = await fetch(API_BASE + url, config);
    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Request failed');
    }

    return data;
}

function isAuthenticated() {
    return !!getToken();
}

function updateNavForAuth() {
    const loginLink = document.getElementById('loginLink');
    const registerLink = document.getElementById('registerLink');
    const profileLink = document.getElementById('profileLink');
    const logoutLink = document.getElementById('logoutLink');
    const createEventLink = document.getElementById('createEventLink');

    const authenticated = isAuthenticated();

    if (loginLink) loginLink.style.display = authenticated ? 'none' : '';
    if (registerLink) registerLink.style.display = authenticated ? 'none' : '';
    if (profileLink) profileLink.style.display = authenticated ? '' : 'none';
    if (logoutLink) logoutLink.style.display = authenticated ? '' : 'none';
}

async function handleLogin(event) {
    event.preventDefault();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('loginError');

    try {
        const data = await apiCall('/api/auth/login', {
            method: 'POST',
            body: { username, password },
        });

        setAuthData(data.token, {
            id: data.id,
            username: data.username,
            email: data.email,
            roles: data.roles,
        });

        window.location.href = '/';
    } catch (error) {
        errorDiv.textContent = error.message || 'Login failed. Please check your credentials.';
        errorDiv.style.display = 'block';
    }

    return false;
}

async function handleRegister(event) {
    event.preventDefault();

    const username = document.getElementById('username').value.trim();
    const email = document.getElementById('email').value.trim();
    const firstName = document.getElementById('firstName').value.trim();
    const lastName = document.getElementById('lastName').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const errorDiv = document.getElementById('registerError');

    if (password !== confirmPassword) {
        errorDiv.textContent = 'Passwords do not match.';
        errorDiv.style.display = 'block';
        return false;
    }

    try {
        await apiCall('/api/auth/signup', {
            method: 'POST',
            body: {
                username,
                email,
                password,
                firstName,
                lastName,
                phone,
            },
        });

        window.location.href = '/login?registered=true';
    } catch (error) {
        errorDiv.textContent = error.message || 'Registration failed. Please try again.';
        errorDiv.style.display = 'block';
    }

    return false;
}

function handleLogout() {
    clearAuth();
    window.location.href = '/';
}

async function loadUserProfile() {
    const errorDiv = document.getElementById('profileError');

    if (!isAuthenticated()) {
        window.location.href = '/login';
        return;
    }

    try {
        const data = await apiCall('/api/users/me', {
            method: 'GET',
        });

        document.getElementById('profileUsername').textContent = data.username || data.name || 'User';
        document.getElementById('profileEmail').textContent = data.email || '';
        document.getElementById('detailUsername').textContent = data.username || '-';
        document.getElementById('detailEmail').textContent = data.email || '-';
        document.getElementById('detailFirstName').textContent = data.firstName || '-';
        document.getElementById('detailLastName').textContent = data.lastName || '-';
        document.getElementById('detailPhone').textContent = data.phoneNumber || data.phone || '-';
        document.getElementById('detailRoles').textContent = (data.roles || []).join(', ') || 'User';

        const initials = (data.firstName && data.lastName)
            ? (data.firstName[0] + data.lastName[0]).toUpperCase()
            : (data.username ? data.username[0].toUpperCase() : 'U');
        document.getElementById('avatarInitials').textContent = initials;
    } catch (error) {
        errorDiv.textContent = 'Failed to load profile. Please try again later.';
        errorDiv.style.display = 'block';
    }
}

async function loadEvents() {
    const container = document.getElementById('eventsContainer');
    const errorDiv = document.getElementById('eventsError');

    try {
        const events = await apiCall('/api/events', {
            method: 'GET',
        });

        if (!events || events.length === 0) {
            container.innerHTML = '<div class="loading">No events found. Check back later!</div>';
            return;
        }

        container.innerHTML = events.map(event => {
            const startDate = event.startDate ? new Date(event.startDate).toLocaleDateString('en-US', {
                year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
            }) : 'TBD';
            const endDate = event.endDate ? new Date(event.endDate).toLocaleDateString('en-US', {
                year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
            }) : 'TBD';

            return `
                <div class="event-card">
                    <h3>${event.name || event.title || 'Untitled Event'}</h3>
                    <p class="event-description">${event.description || 'No description available.'}</p>
                    <div class="event-meta">
                        <span>${event.location || 'Location TBD'}</span>
                        <span>${startDate} - ${endDate}</span>
                        <span>Capacity: ${event.capacity || event.maxParticipants || 'Unlimited'}</span>
                    </div>
                    <div class="event-actions">
                        <a href="/events/${event.id}" class="btn btn-primary">View Details</a>
                    </div>
                </div>
            `;
        }).join('');
    } catch (error) {
        errorDiv.textContent = 'Failed to load events. Please make sure the services are running.';
        errorDiv.style.display = 'block';
        container.innerHTML = '';
    }
}

async function loadEventDetail() {
    const id = window.location.pathname.split('/').pop();
    const container = document.getElementById('eventDetail');
    const registerSection = document.getElementById('registerSection');
    const errorDiv = document.getElementById('eventError');

    try {
        const event = await apiCall('/api/events/' + id, {
            method: 'GET',
        });

        const startDate = event.startDate ? new Date(event.startDate).toLocaleDateString('en-US', {
            year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
        }) : 'TBD';
        const endDate = event.endDate ? new Date(event.endDate).toLocaleDateString('en-US', {
            year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
        }) : 'TBD';

        container.innerHTML = `
            <h1>${event.name || event.title || 'Untitled Event'}</h1>
            <p class="event-description">${event.description || 'No description available.'}</p>
            <div class="event-info">
                <div class="info-item">
                    <span class="info-label">Location</span>
                    <span class="info-value">${event.location || 'TBD'}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Start Date</span>
                    <span class="info-value">${startDate}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">End Date</span>
                    <span class="info-value">${endDate}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Capacity</span>
                    <span class="info-value">${event.capacity || event.maxParticipants || 'Unlimited'}</span>
                </div>
            </div>
        `;

        if (isAuthenticated()) {
            registerSection.style.display = 'block';
        }
    } catch (error) {
        errorDiv.textContent = 'Failed to load event details.';
        errorDiv.style.display = 'block';
        container.innerHTML = '';
    }
}

async function handleRegisterForEvent() {
    const id = window.location.pathname.split('/').pop();
    const statusDiv = document.getElementById('registrationStatus');
    const registerSection = document.getElementById('registerSection');
    const user = getUser();

    if (!user || !user.email) {
        statusDiv.className = 'registration-status error';
        statusDiv.textContent = 'Please login first to register for events.';
        statusDiv.style.display = 'block';
        return;
    }

    try {
        const data = await apiCall('/api/registrations', {
            method: 'POST',
            body: {
                eventId: parseInt(id),
                participantName: user.username || user.name || 'User',
                participantEmail: user.email,
            },
        });

        statusDiv.className = 'registration-status success';
        statusDiv.textContent = 'Successfully registered for this event!';
        statusDiv.style.display = 'block';
        registerSection.style.display = 'none';
    } catch (error) {
        statusDiv.className = 'registration-status error';
        statusDiv.textContent = error.message || 'Registration failed. You may already be registered.';
        statusDiv.style.display = 'block';
    }
}

async function handleCreateEvent(event) {
    event.preventDefault();

    const name = document.getElementById('name').value.trim();
    const description = document.getElementById('description').value.trim();
    const location = document.getElementById('location').value.trim();
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const capacity = parseInt(document.getElementById('capacity').value);
    const errorDiv = document.getElementById('createEventError');

    if (!isAuthenticated()) {
        errorDiv.textContent = 'Please login to create events.';
        errorDiv.style.display = 'block';
        return false;
    }

    try {
        await apiCall('/api/events', {
            method: 'POST',
            body: {
                name,
                description,
                location,
                startDate: new Date(startDate).toISOString(),
                endDate: new Date(endDate).toISOString(),
                capacity,
            },
        });

        window.location.href = '/events';
    } catch (error) {
        errorDiv.textContent = error.message || 'Failed to create event.';
        errorDiv.style.display = 'block';
    }

    return false;
}

document.addEventListener('DOMContentLoaded', function () {
    updateNavForAuth();

    const logoutLink = document.getElementById('logoutLink');
    if (logoutLink) {
        logoutLink.addEventListener('click', function (e) {
            e.preventDefault();
            handleLogout();
        });
    }

    const params = new URLSearchParams(window.location.search);
    if (params.get('registered') === 'true') {
        const loginError = document.getElementById('loginError');
        if (loginError) {
            loginError.className = 'form-success';
            loginError.textContent = 'Registration successful! Please login with your credentials.';
            loginError.style.display = 'block';
        }
    }

    const path = window.location.pathname;

    if (path === '/profile') {
        loadUserProfile();
    } else if (path === '/events') {
        loadEvents();
    } else if (path.match(/^\/events\/\d+$/)) {
        loadEventDetail();
    }
});
