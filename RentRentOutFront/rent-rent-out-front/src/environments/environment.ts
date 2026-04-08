const isBrowser = typeof window !== 'undefined';
const proto = isBrowser && window.location.protocol === 'https:' ? 'wss:' : 'ws:';
const host = isBrowser ? window.location.host : 'localhost';

export const environment = {
  production: false,
  apiUrl: '/api',
  wsUrl: `${proto}//${host}/ws`,
  googleClientId: '1030670787389-cftefckkjmpgiv41okb87oatffou4e5k.apps.googleusercontent.com',
};
