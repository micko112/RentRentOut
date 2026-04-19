import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.izdajemiznajmljujem.app',
  appName: 'IzdajemIznajmljujem',
  webDir: 'dist/rent-rent-out-front/browser',

  server: {
    androidScheme: 'https',
    allowNavigation: ['izdajemiznajmljujem.com'],
  },

  plugins: {
    SplashScreen: {
      launchShowDuration: 2000,
      launchAutoHide: true,
      backgroundColor: '#813181',
      androidSplashResourceName: 'splash',
      androidScaleType: 'CENTER_CROP',
      showSpinner: false,
    },
    StatusBar: {
      style: 'LIGHT',
      backgroundColor: '#813181',
    },
    Keyboard: {
      resize: 'body',
      resizeOnFullScreen: true,
    },
    CapacitorHttp: {
      enabled: true,
    },
    CapacitorCookies: {
      enabled: true,
    },
  },

  android: {
    buildOptions: {
      keystorePath: 'izdajem-iznajmljujem.keystore',
      keystoreAlias: 'izdajem',
    },
  },

  ios: {
    scheme: 'IzdajemIznajmljujem',
  },
};

export default config;
