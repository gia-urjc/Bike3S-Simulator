import * as Rx from 'rxjs/operators';

const operators = (window as any).require('rxjs/operators');

export const takeWhile: typeof Rx.takeWhile = operators.takeWhile;
