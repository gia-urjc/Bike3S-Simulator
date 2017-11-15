import {Injectable} from '@angular/core';
import {ipcRenderer, Event} from 'electron'
import { Observable } from 'rxjs/Observable';
import * as Rx from 'rxjs';

@Injectable()
export class MainComunicator {
    
    test:string[] = [];

    constructor(){

    }

    //Get
     getDataTest(num: number):Observable<any>{
        ipcRenderer.send('get-data-test', num);
        return Rx.Observable.from(this.test);   
        
    }

    init(){
        ipcRenderer.on('get-data-reply', (event: Event, arg: any) => {
            this.test.push(arg);
        })
    }

    
}