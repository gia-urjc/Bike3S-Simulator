import {Injectable} from '@angular/core';
const {ipcRenderer, Event} = (window as any).require('electron'); // https://github.com/electron/electron/issues/7300#issuecomment-274269710
import { Observable } from 'rxjs/Observable';
const Rx = (window as any).require('rxjs/Rx');

@Injectable()
export class MainCommunicator {

    private dataMap = new Map<string, any>();

    public getData(messageId: string, args: any): Promise<any> {
        let promise = new Promise((resolve: any, reject: any) => {
            ipcRenderer.on(messageId.concat('-reply'), (event: Event, arg: any) => {
                this.dataMap.set(messageId, arg);
                return resolve(this.dataMap.get(messageId));
            })
            ipcRenderer.send(messageId.concat('-get'), args);
        })
        promise.then((data: any) => {
            ipcRenderer.removeAllListeners(messageId.concat('-reply'));
        })
        return promise;
    }

}
