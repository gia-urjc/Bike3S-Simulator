import { Injectable } from '@angular/core';
import { AjaxProtocol, AjaxHistory } from './AjaxProtocol';

// https://github.com/electron/electron/issues/7300#issuecomment-274269710
const { ipcRenderer, Event } = (window as any).require('electron');

function readIpc(channel: string, ...requestArgs: Array<any>): Promise<any> {
    ipcRenderer.send(channel, ...requestArgs);
    return new Promise((resolve, reject) => {
        ipcRenderer.on(channel, (event: Event, response: { status: number, data?: any }) => {
            ipcRenderer.removeAllListeners(channel);
            if (response.status === 200) {
                resolve(response.data);
            } else {
                reject(response.data);
            }
        });
    });
}

class ElectronHistory implements AjaxHistory {
    init(path: string): Promise<void> {
        return readIpc('history-init', path);
    }

    readEntities(): Promise<object> {
        return readIpc('history-entities');
    }

    numberOFChangeFiles(): Promise<number> {
        return readIpc('history-nchanges');
    }

    previousChangeFile(): Promise<object> {
        return readIpc('history-previous');
    }

    nextChangeFile(): Promise<object> {
        return readIpc('history-next');
    }
}

@Injectable()
export class ElectronAjax implements AjaxProtocol {

    history: AjaxHistory;

    constructor() {
        this.history = new ElectronHistory();
    }
}
