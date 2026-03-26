import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import * as Sentry from "@sentry/angular";

// Sentry MORA biti inicijalizovan PRE bootstrapApplication
// da bi hvatao greške tokom pokretanja aplikacije
Sentry.init({
  dsn: "https://7eeb8019f49427a47207eed49a4a4ee9@o4511110766788608.ingest.de.sentry.io/4511110865944656",
  integrations: [
    Sentry.browserTracingIntegration(),
    Sentry.replayIntegration(),
  ],
  tracesSampleRate: 1.0,
  replaysSessionSampleRate: 0.1,  // 10% svih sesija
  replaysOnErrorSampleRate: 1.0,  // 100% sesija sa greškom
});

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));

if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/sw.js').catch((err) => {
    console.error('Service worker registration failed:', err);
  });
}
