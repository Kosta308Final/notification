const FALLBACK_URL = '/';

self.addEventListener('push', event => {
  event.waitUntil((async () => {
    let data = {};
    try { data = event.data ? event.data.json() : {}; } catch (_) { data = { body: event.data?.text() || '' }; }
    const title = data.title || '새 알림';
    const options = {
      body: data.body || data.content || '',
      tag: data.notificationId ? `notification-${data.notificationId}` : undefined,
      data: { notificationId: data.notificationId, actionUrl: data.actionUrl || null },
      renotify: Boolean(data.importance === 'URGENT'),
      icon: '/favicon.ico'
    };
    const clients = await self.clients.matchAll({ type: 'window', includeUncontrolled: true });
    const active = clients.find(client => client.visibilityState === 'visible');
    if (active) active.postMessage({ type: 'NOTIFICATION_RECEIVED', notification: data });
    else await self.registration.showNotification(title, options);
  })());
});

self.addEventListener('notificationclick', event => {
  event.notification.close();
  event.waitUntil((async () => {
    const requested = event.notification.data?.actionUrl || FALLBACK_URL;
    let target;
    try {
      target = new URL(requested, self.location.origin);
      if (target.origin !== self.location.origin || target.protocol !== 'http:' && target.protocol !== 'https:') target = new URL(FALLBACK_URL, self.location.origin);
    } catch (_) { target = new URL(FALLBACK_URL, self.location.origin); }
    const pages = await self.clients.matchAll({ type: 'window', includeUncontrolled: true });
    const existing = pages.find(page => page.url.startsWith(self.location.origin));
    if (existing) { await existing.navigate(target.href); return existing.focus(); }
    return self.clients.openWindow(target.href);
  })());
});
