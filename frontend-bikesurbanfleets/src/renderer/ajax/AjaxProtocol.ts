import { HistoryEntitiesJson, HistoryTimeEntry } from '../../shared/history';
import { JsonValue } from '../../shared/util';

export interface HistoryAjax {
    init(path: string): Promise<void>,
    getEntities(type: string): Promise<HistoryEntitiesJson>,
    numberOFChangeFiles(): Promise<number>,
    previousChangeFile(): Promise<Array<HistoryTimeEntry>>,
    nextChangeFile(): Promise<Array<HistoryTimeEntry>>,
    getChangeFile(n: number): Promise<Array<HistoryTimeEntry>>,
}

export interface SettingsAjax {
    get(property: string): Promise<any>,
    set(property: string, value: JsonValue): Promise<void>,
}

export interface AjaxProtocol {
    history: HistoryAjax,
    settings: SettingsAjax,
}
