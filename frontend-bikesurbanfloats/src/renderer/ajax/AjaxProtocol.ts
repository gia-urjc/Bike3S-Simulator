import { JsonValue, PlainObject } from '../../shared/util';

export interface HistoryAjax {
    init(path: string): Promise<void>,
    readEntities(): Promise<PlainObject>,
    numberOFChangeFiles(): Promise<number>,
    previousChangeFile(): Promise<PlainObject>,
    nextChangeFile(): Promise<PlainObject>,
}

export interface SettingsAjax {
    get(property: string): Promise<any>,
    set(property: string, value: JsonValue): Promise<void>,
}

export interface AjaxProtocol {
    history: HistoryAjax,
    settings: SettingsAjax,
}
