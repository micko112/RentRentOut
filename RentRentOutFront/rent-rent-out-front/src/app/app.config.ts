import {ApplicationConfig, LOCALE_ID, provideZoneChangeDetection} from '@angular/core';
import { provideRouter } from '@angular/router';
import {HttpClientModule, provideHttpClient} from '@angular/common/http';
import { importProvidersFrom } from '@angular/core';
import localeSr from '@angular/common/locales/sr-Latn';

import { routes } from './app.routes';
import {registerLocaleData} from '@angular/common';

registerLocaleData(localeSr)
export const appConfig: ApplicationConfig = {
  providers:
    [provideZoneChangeDetection({ eventCoalescing: true }),
      provideRouter(routes),
      provideHttpClient(),
      {provide: LOCALE_ID, useValue: 'sr-Latn'},]
};
