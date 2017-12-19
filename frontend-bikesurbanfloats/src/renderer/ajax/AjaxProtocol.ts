import { HistoryEntities, HistoryTimeEntries } from '../../shared/history';
import { JsonValue } from '../../shared/util';

export interface HistoryAjax {
    init(path: string): Promise<void>,
    readEntities(): Promise<HistoryEntities>,
    numberOFChangeFiles(): Promise<number>,
    previousChangeFile(): Promise<HistoryTimeEntries>,
    nextChangeFile(): Promise<HistoryTimeEntries>,
}

export interface SettingsAjax {
    get(property: string): Promise<any>,
    set(property: string, value: JsonValue): Promise<void>,
}

export interface AjaxProtocol {
    history: HistoryAjax,
    settings: SettingsAjax,
}
