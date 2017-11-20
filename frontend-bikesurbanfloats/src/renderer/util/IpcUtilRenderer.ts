import {Injectable} from '@angular/core';
const {ipcRenderer, Event} = (window as any).require('electron'); // https://github.com/electron/electron/issues/7300#issuecomment-274269710
import { Observable } from 'rxjs/Observable';
const Rx = (window as any).require('rxjs/Rx');

@Injectable()
export class IpcUtilRenderer {

    private dataMap = new Map<string, any>();

    public getData(messageId: string, args: any): Promise<any> {
        let promise = new Promise((resolve: any, reject: any) => {
            ipcRenderer.once(messageId, (event: Event, data: any) => {
                return resolve(data);
            })
            ipcRenderer.send(messageId, args);
        })
        return promise;
    }
}
