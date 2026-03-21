
export const API_BASE_URL = 'http://localhost:8080/api';
export const WS_BASE_URL = API_BASE_URL.replace(/^http/, 'ws').replace('/api', '/ws');

// VAPID public key — must match app.vapid.public-key in application.properties
export const VAPID_PUBLIC_KEY = 'BEl62iUYgUivxIkv69yViEuiBIa-Ib9-SkvMeAtA3LFgDzkrxZJjSgSnfckjBJuBkr3qBUYIHBQFLXYp5Nksh8U';
