import { Injectable } from '@angular/core';
import { Event } from 'electron';
import { HistoryEntitiesJson, HistoryTimeEntry } from '../../shared/history';
import { JsonValue } from '../../shared/util';
import {AjaxProtocol, FormSchemaAjax, HistoryAjax, SettingsAjax} from './AjaxProtocol';
import {EntryPointDataType} from "../../shared/configuration";

// https://github.com/electron/electron/issues/7300#issuecomment-274269710
const { ipcRenderer } = (window as any).require('electron');

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

class ElectronHistory implements HistoryAjax {

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

    async getEntities(type: string): Promise<HistoryEntitiesJson> {
        if (!this.ready) throw ElectronHistory.NOT_READY;
        return await readIpc('history-entities', type);
    }

    async numberOFChangeFiles(): Promise<number> {
        if (!this.ready) throw ElectronHistory.NOT_READY;
        return await readIpc('history-nchanges');
    }

    async previousChangeFile(): Promise<Array<HistoryTimeEntry>> {
        if (!this.ready) throw ElectronHistory.NOT_READY;
        return await readIpc('history-previous');
    }

    async nextChangeFile(): Promise<Array<HistoryTimeEntry>> {
        if (!this.ready) throw ElectronHistory.NOT_READY;
        return await readIpc('history-next');
    }

    async getChangeFile(n: number): Promise<Array<HistoryTimeEntry>> {
        if (!this.ready) throw ElectronHistory.NOT_READY;
        return await readIpc('history-get', n);
    }

    async closeHistory(): Promise<void> {
        if(!this.ready) throw ElectronHistory.NOT_READY;
        await readIpc('history-close');
        this.ready = false;
    }

}

class ElectronSettings implements SettingsAjax {
    async get(property: string): Promise<any> {
        return await readIpc('settings-get', property);
    }

    async set(property: string, value: JsonValue): Promise<void> {
        await readIpc('settings-set', property, value);
    }
}

class ElectronFormSchema implements FormSchemaAjax {
    async init(): Promise<any> {
        return await readIpc('form-schema-init');
    }

    async getSchemaFormEntryPointAndUserTypes(): Promise<any> {
        return await readIpc('form-schema-entry-user-type');
    }

    async getSchemaByTypes(dataTypes: EntryPointDataType): Promise<any> {
        return await readIpc('form-schema-entry-point-by-type', dataTypes);
    }

    async getStationSchema(): Promise<any> {
        return await readIpc('form-schema-station');
    }
}

@Injectable()
export class ElectronAjax implements AjaxProtocol {

    history: HistoryAjax;
    settings: SettingsAjax;
    formSchema: FormSchemaAjax;

    constructor() {
        this.history = new ElectronHistory();
        this.settings = new ElectronSettings();
        this.formSchema = new ElectronFormSchema();
    }
}
