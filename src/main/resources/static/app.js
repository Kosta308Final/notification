(function () {
  'use strict';
  const state = { page: 0, hasNext: true, filter: '', notifications: [], pushIds: new Set() };
  const $ = id => document.getElementById(id);
  const token = () => window.localStorage.getItem('notification.jwt') || '';
  const api = async (url, options = {}) => {
    const headers = { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...(options.headers || {}) };
    if (token()) headers.Authorization = `Bearer ${token()}`;
    const response = await fetch(url, { ...options, headers });
    if (!response.ok) throw new Error(response.status === 401 ? '로그인이 필요합니다.' : `요청 실패 (${response.status})`);
    return response.status === 204 ? null : response.json();
  };
  const setStatus = message => { $('status').textContent = message || ''; };
  const urlBase64ToUint8Array = value => Uint8Array.from(atob(value.replace(/-/g, '+').replace(/_/g, '/') + '='.repeat((4 - value.length % 4) % 4)), c => c.charCodeAt(0));
  const browserName = () => /Edg/.test(navigator.userAgent) ? 'Edge' : /Chrome/.test(navigator.userAgent) ? 'Chrome' : /Safari/.test(navigator.userAgent) ? 'Safari' : 'Browser';
  const updateUnreadCount = async () => { try { $('unread-count').textContent = await api('/api/notifications/unread-count'); } catch (_) {} };
  const render = () => {
    $('notifications').innerHTML = state.notifications.map(n => `<article class="notification ${n.read ? 'read' : 'unread'} ${n.importance === 'URGENT' ? 'urgent' : ''}" data-id="${n.id}"><div class="notification-head"><h3>${escapeHtml(n.title)}${n.read ? '' : ' <span aria-label="읽지 않음">•</span>'}</h3><time>${formatDate(n.createdAt)}</time></div><div class="notification-body">${escapeHtml(n.content)}${n.actionUrl ? `<div class="notification-actions"><a href="${safeUrl(n.actionUrl)}">바로가기</a></div>` : ''}</div></article>`).join('');
    $('more').hidden = !state.hasNext;
  };
  const escapeHtml = value => String(value ?? '').replace(/[&<>'"]/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', "'":'&#39;', '"':'&quot;' }[c]));
  const safeUrl = value => { try { const u = new URL(value, location.origin); return u.origin === location.origin ? u.href : '#'; } catch (_) { return '#'; } };
  const formatDate = value => value ? new Date(value).toLocaleString('ko-KR') : '';
  async function load(reset = false) { if (reset) { state.page = 0; state.notifications = []; } if (!state.hasNext && !reset) return; try { const query = new URLSearchParams({ page: state.page, size: 20 }); if (state.filter) query.set('isRead', state.filter); const result = await api(`/api/notifications?${query}`); state.notifications.push(...result.content); state.hasNext = !result.last; state.page++; render(); } catch (error) { setStatus(error.message); } }
  async function enablePush() {
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) return setStatus('이 브라우저는 웹 푸시를 지원하지 않습니다.');
    if (!token()) return setStatus('먼저 로그인 토큰을 등록해야 합니다.');
    const button = $('enable-push'); button.disabled = true;
    try { const permission = await Notification.requestPermission(); if (permission !== 'granted') throw new Error(permission === 'denied' ? '브라우저 설정에서 알림 권한을 허용해 주세요.' : '알림 권한을 허용해야 합니다.'); const registration = await navigator.serviceWorker.register('/sw.js'); const key = (await api('/api/notifications/subscriptions/vapid-public-key')).publicKey; let subscription = await registration.pushManager.getSubscription(); if (!subscription) subscription = await registration.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey: urlBase64ToUint8Array(key) }); const json = subscription.toJSON(); await api('/api/notifications/subscriptions', { method: 'POST', body: JSON.stringify({ endpoint: json.endpoint, p256dh: json.keys.p256dh, auth: json.keys.auth, browser: browserName(), deviceType: /Mobi|Android/i.test(navigator.userAgent) ? 'MOBILE' : 'PC' }) }); setStatus('알림 수신이 활성화되었습니다.'); $('permission').hidden = true; } catch (error) { setStatus(error.message); } finally { button.disabled = false; }
  }
  async function logout() { try { const registration = await navigator.serviceWorker?.getRegistration(); const subscription = await registration?.pushManager.getSubscription(); if (subscription && token()) await api('/api/notifications/subscriptions/deactivate', { method: 'POST', body: JSON.stringify({ endpoint: subscription.endpoint }) }); await subscription?.unsubscribe(); } catch (_) {} window.localStorage.removeItem('notification.jwt'); setStatus('로그아웃되었습니다.'); }
  $('filter').addEventListener('change', e => { state.filter = e.target.value; state.hasNext = true; load(true); }); $('more').addEventListener('click', () => load()); $('enable-push').addEventListener('click', enablePush); $('logout').addEventListener('click', logout); $('read-all').addEventListener('click', async () => { try { await api('/api/notifications/read-all', { method: 'PATCH' }); state.notifications.forEach(n => n.read = true); render(); await updateUnreadCount(); } catch (error) { setStatus(error.message); } });
  $('notifications').addEventListener('click', async e => { const item = e.target.closest('.notification'); if (!item) return; const n = state.notifications.find(value => String(value.id) === item.dataset.id); if (!n) return; item.classList.toggle('expanded'); if (!n.read) { try { await api(`/api/notifications/${n.id}/read`, { method: 'PATCH' }); n.read = true; item.classList.remove('unread'); item.classList.add('read'); await updateUnreadCount(); } catch (error) { setStatus(error.message); } } });
  navigator.serviceWorker?.addEventListener('message', event => { if (event.data?.type === 'NOTIFICATION_RECEIVED') { const id = event.data.notification?.notificationId; if (id && state.pushIds.has(id)) return; if (id) state.pushIds.add(id); setStatus('새 알림이 도착했습니다.'); state.hasNext = true; load(true); updateUnreadCount(); } });
  (async () => { const permission = 'Notification' in window ? Notification.permission : 'denied'; $('permission').hidden = permission === 'granted'; if (permission === 'denied') $('permission-text').textContent = '브라우저 설정에서 알림 권한을 허용할 수 있습니다.'; await load(true); await updateUnreadCount(); if (permission === 'granted' && token()) await enablePush(); })();
})();
