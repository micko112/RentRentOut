import { mergeApplicationConfig, ApplicationConfig, ErrorHandler } from '@angular/core';
import { provideServerRendering } from '@angular/platform-server';
import { appConfig } from './app.config';

const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(),
    // Override Sentry ErrorHandler na serveru — Sentry je browser-only
    { provide: ErrorHandler, useValue: new ErrorHandler() },
  ]
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
