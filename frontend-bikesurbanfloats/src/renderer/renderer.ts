import 'core-js/es6';
import 'core-js/es7/reflect';
import 'zone.js/dist/zone'; // if any issues arise use zone-mix instead

import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { enableProdMode } from '@angular/core';

import { AppModule } from './app/app.module';

if (process.env.target === 'development') {
    enableProdMode();
} else {
    // Development and test
    Error['stackTraceLimit'] = Infinity;
    require('zone.js/dist/long-stack-trace-zone');
}

platformBrowserDynamic().bootstrapModule(AppModule);
