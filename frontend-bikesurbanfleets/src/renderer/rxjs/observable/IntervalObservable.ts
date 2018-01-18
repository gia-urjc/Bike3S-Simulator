import * as Rx from 'rxjs/observable/IntervalObservable';

const observable = (window as any).require('rxjs/observable/IntervalObservable');

export const IntervalObservable: typeof Rx.IntervalObservable = observable.IntervalObservable;
