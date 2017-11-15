import {Injectable} from '@angular/core';
const {ipcRenderer, Event} = (window as any).require('electron'); // https://github.com/electron/electron/issues/7300#issuecomment-274269710
import { Observable } from 'rxjs/Observable';
const Rx = (window as any).require('rxjs/Rx');

@Injectable()
export class MainComunicator {

    test: string[] = [];

    constructor() {
        this.init();
    }

    // Get
     getDataTest(num: number): Observable<any> {
        ipcRenderer.send('get-data-test', num);
        return Rx.Observable.of(this.test);
    }

    init() {
        ipcRenderer.on('get-data-reply', (event: Event, arg: any) => {
            this.test.push(arg);
        })
    }

}
