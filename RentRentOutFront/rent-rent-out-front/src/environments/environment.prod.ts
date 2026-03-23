const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';

export const environment = {
  production: true,
  apiUrl: '/api',
  wsUrl: `${proto}//${window.location.host}/ws`,
  googleClientId: '1030670787389-cftefckkjmpgiv41okb87oatffou4e5k.apps.googleusercontent.com',
};
