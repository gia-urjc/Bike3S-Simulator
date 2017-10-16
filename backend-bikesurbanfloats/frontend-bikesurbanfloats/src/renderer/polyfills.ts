import 'core-js/es6';
import 'core-js/es7/reflect';
import 'zone.js/dist/zone-mix';

if (process.env.target === 'production') {
    // Production
} else {
    // Development and test
    Error['stackTraceLimit'] = Infinity;
    require('zone.js/dist/long-stack-trace-zone');
}
