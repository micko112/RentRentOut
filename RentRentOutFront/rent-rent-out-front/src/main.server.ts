import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { config } from './app/app.config.server';
import type { PlatformRef } from '@angular/core';

// Angular 19.2+: bootstrap mora da prihvati context ({ platformRef }) koji
// renderApplication iz @angular/platform-server prosleđuje pri SSR renderovanju.
const bootstrap = (context?: { platformRef: PlatformRef }) =>
  bootstrapApplication(AppComponent, config, context);

export default bootstrap;
