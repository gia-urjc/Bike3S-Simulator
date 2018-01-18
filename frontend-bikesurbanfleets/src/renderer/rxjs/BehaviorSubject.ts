import * as Rx from 'rxjs/BehaviorSubject';

const rxjs = (window as any).require('rxjs/BehaviorSubject');

export const BehaviorSubject: typeof Rx.BehaviorSubject = rxjs.BehaviorSubject;
