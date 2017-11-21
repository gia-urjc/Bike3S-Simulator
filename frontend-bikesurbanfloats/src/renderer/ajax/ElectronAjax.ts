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

    private static readonly IS_READY = new Error(`HistoryReady has already been initialized!`);
    private static readonly NOT_READY = new Error(`HistoryReader hasn't been initialized yet!`);

    private ready = false;

    async init(path: string): Promise<void> {
        if (this.ready) throw ElectronHistory.IS_READY;
        await readIpc('history-init', path);
        this.ready = true;
    }

    async close(): Promise<void> {
        if (!this.ready) throw ElectronHistory.NOT_READY;
        await readIpc('history-close');
        this.ready = false;
    }

    async readEntities(): Promise<object> {
        if (!this.ready) throw ElectronHistory.NOT_READY;
        return await readIpc('history-entities');
    }

    async numberOFChangeFiles(): Promise<number> {
        if (!this.ready) throw ElectronHistory.NOT_READY;
        return await readIpc('history-nchanges');
    }

    async previousChangeFile(): Promise<object> {
        if (!this.ready) throw ElectronHistory.NOT_READY;
        return await readIpc('history-previous');
    }

    async nextChangeFile(): Promise<object> {
        if (!this.ready) throw ElectronHistory.NOT_READY;
        return await readIpc('history-next');
    }
}

@Injectable()
export class ElectronAjax implements AjaxProtocol {

    history: AjaxHistory;

    constructor() {
        this.history = new ElectronHistory();
    }
}
