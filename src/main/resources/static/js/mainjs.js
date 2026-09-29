'use strict';
// AJAX with fetch: same-origin requests, no CDN dependency.
const TOKEN_KEY = 'jwt_nimbus_token';
const EXPIRY_KEY = 'jwt_nimbus_expiry';
const byId = (id) => document.getElementById(id);
function feedback(message, success = false) {
  const box = byId('feedback');
  box.textContent = message;
  box.className = success ? 'success' : '';
  box.hidden = false;
}
function clearToken() {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(EXPIRY_KEY);
}
async function api(path, options = {}, protectedRequest = false) {
  const headers = { ...options.headers };
  if (options.body) headers['Content-Type'] = 'application/json';
  if (protectedRequest) {
    const token = sessionStorage.getItem(TOKEN_KEY);
    if (!token) { location.replace('/login?expired=1'); throw new Error('Vui lòng đăng nhập.'); }
    headers.Authorization = `Bearer ${token}`;
  }
  const response = await fetch(path, { ...options, headers, cache: 'no-store' });
  const data = await response.json().catch(() => ({}));
  if (protectedRequest && (response.status === 401 || response.status === 403)) {
    clearToken(); location.replace('/login?expired=1');
  }
  if (!response.ok) throw new Error(data.message || `Yêu cầu thất bại (HTTP ${response.status}).`);
  return data;
}
function submit(formId, action) {
  byId(formId)?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const button = event.currentTarget.querySelector('button[type="submit"]');
    button.disabled = true;
    try { await action(); }
    catch (error) { feedback(error.message || 'Không kết nối được máy chủ.'); }
    finally { button.disabled = false; }
  });
}
const page = document.body.dataset.page;
if (page === 'login') {
  const query = new URLSearchParams(location.search);
  if (query.has('registered')) feedback('Đăng ký thành công. Bạn có thể đăng nhập.', true);
  if (query.has('expired')) feedback('Phiên không còn hợp lệ. Vui lòng đăng nhập lại.');
  submit('login-form', async () => {
    const data = await api('/auth/login', {method:'POST', body:JSON.stringify({
      email:byId('email').value.trim(), password:byId('password').value
    })});
    sessionStorage.setItem(TOKEN_KEY, data.token);
    sessionStorage.setItem(EXPIRY_KEY, String(Date.now() + data.expiresIn));
    location.assign('/user/profile');
  });
}
if (page === 'register') {
  submit('register-form', async () => {
    await api('/auth/signup', {method:'POST', body:JSON.stringify({
      fullName:byId('fullName').value.trim(), email:byId('email').value.trim(), password:byId('password').value
    })});
    location.assign('/login?registered=1');
  });
}
const formatDate = (value) => value ? new Date(value).toLocaleString('vi-VN') : '—';
async function loadProfile() {
  const user = await api('/users/me', {}, true);
  byId('fullName').textContent = user.fullName;
  byId('email').textContent = user.email;
  byId('userId').textContent = user.id;
  byId('createdAt').textContent = formatDate(user.createdAt);
  byId('updatedAt').textContent = formatDate(user.updatedAt);
  byId('profile').textContent = JSON.stringify(user, null, 2);
  const expiry = sessionStorage.getItem(EXPIRY_KEY);
  byId('expiresAt').textContent = expiry ? formatDate(Number(expiry)) : '—';
  // Images are local server-controlled assets in this assignment.
  byId('images').src = '/images/avatar.svg';
}
if (page === 'profile') {
  loadProfile().catch(error => feedback(error.message));
  byId('reload-profile').addEventListener('click', () => loadProfile().catch(error => feedback(error.message)));
  byId('logout').addEventListener('click', () => { clearToken(); location.replace('/login'); });
  byId('load-users').addEventListener('click', async () => {
    const button = byId('load-users'); button.disabled = true;
    try {
      const users = await api('/users', {}, true);
      const body = byId('users-body'); body.replaceChildren();
      users.forEach(user => {
        const row = document.createElement('tr');
        [user.id, user.fullName, user.email, formatDate(user.createdAt)].forEach(value => {
          const cell = document.createElement('td'); cell.textContent = value; row.appendChild(cell);
        });
        body.appendChild(row);
      });
    } catch(error) { feedback(error.message); }
    finally { button.disabled = false; }
  });
}
