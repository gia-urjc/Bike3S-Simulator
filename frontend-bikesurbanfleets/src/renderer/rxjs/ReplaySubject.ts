import * as Rx from 'rxjs/ReplaySubject';

const rxjs = (window as any).require('rxjs/ReplaySubject');

export const ReplaySubject: typeof Rx.ReplaySubject = rxjs.ReplaySubject;